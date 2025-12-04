package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.general.dtos.ApiResponse;
import com.semicolon.africa.tapprbackend.general.enums.VerificationStatus;
import com.semicolon.africa.tapprbackend.general.messages.ErrorMessages;
import com.semicolon.africa.tapprbackend.general.messages.SuccessMessages;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.tapprException.TapprException;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.models.VerificationToken;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.data.repositories.VerificationTokenRepository;
import com.semicolon.africa.tapprbackend.onboarding.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.onboarding.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.services.interfaces.UserService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.EmailService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.VerificationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private static final int RESEND_COOLDOWN_SECONDS = 60;



    @Transactional
    @Override
    public ApiResponse<CreateNewUserResponse> register(CreateNewUserRequest request) {
        try {
            validateSignUpRequest(request);

            if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
                return ApiResponse.failure(ErrorMessages.EMAIL_ALREADY_EXISTS);
            }
            if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                return ApiResponse.failure(ErrorMessages.PHONE_ALREADY_EXISTS);
            }

            User user = new User();
            user.setFirstName(request.getFirstName().trim());
            user.setLastName(request.getLastName().trim());
            user.setEmail(request.getEmail().toLowerCase().trim());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPhoneNumber(request.getPhoneNumber().trim());
            user.setRole(Role.REGULAR);
            user.setKycVerified(false);
            user.setVerified(false);
            user.setLoggedIn(false);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            String otp = verificationTokenService.generateToken(user);
            emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), otp);

            CreateNewUserResponse resp = new CreateNewUserResponse();
            resp.setUserId(user.getId().toString());
            resp.setEmail(user.getEmail());
            resp.setPhoneNumber(user.getPhoneNumber());
            resp.setMessage(SuccessMessages.USER_REGISTERED);

            return ApiResponse.success(SuccessMessages.EMAIL_SENT, resp);

        } catch (IllegalArgumentException ex) {
            log.warn("Registration validation failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Registration error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    @Override
    public ApiResponse<VerificationStatus> validateToken(String email, String otp) {

        Optional<VerificationToken> optionalToken =
                verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(email, otp);

        if (optionalToken.isEmpty()) {
            return ApiResponse.failure(ErrorMessages.INVALID_TOKEN, VerificationStatus.INVALID);
        }

        VerificationToken token = optionalToken.get();

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ApiResponse.failure(ErrorMessages.TOKEN_EXPIRED, VerificationStatus.EXPIRED);
        }

        if (token.isUsed()) {
            return ApiResponse.failure(ErrorMessages.INVALID_TOKEN, VerificationStatus.INVALID);
        }

        token.setUsed(true);
        verificationTokenRepository.save(token);

        User user = token.getUser();
        user.setVerified(true);
        userRepository.save(user);

        return ApiResponse.success(SuccessMessages.EMAIL_VERIEFIED_SUCCESSFULLY, VerificationStatus.ACTIVE);
    }


    @Transactional
    @Override
    public ApiResponse<LoginResponse> verifyEmailAndLogin(String email, String otp) {

        try {
            VerificationStatus status = verificationTokenService.validateToken(email, otp);

            if (status == VerificationStatus.INVALID)
                return ApiResponse.failure(ErrorMessages.INVALID_TOKEN);

            if (status == VerificationStatus.EXPIRED)
                return ApiResponse.failure(ErrorMessages.TOKEN_EXPIRED);

            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            user.setVerified(true);
            user.setLoggedIn(true);
            userRepository.save(user);

            String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

            LoginResponse response = new LoginResponse(
                    SuccessMessages.USER_VERIFIED_AND_LOGGED_IN,
                    accessToken,
                    refreshToken,
                    user.getRole(),
                    true,
                    user.getId().toString()
            );

            return ApiResponse.success(SuccessMessages.USER_VERIFIED_AND_LOGGED_IN, response);

        } catch (Exception ex) {
            log.error("Verify & login error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }


    @Transactional
    @Override
    public ApiResponse<String> resendVerificationOtp(String email) {
        try {
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new TapprException(ErrorMessages.USER_NOT_FOUND));

            if (user.isVerified()) return ApiResponse.failure(SuccessMessages.EMAIL_VERIFIED);

            Optional<VerificationToken> optLast = verificationTokenRepository
                    .findByUserId(user.getId());

            if (optLast.isPresent()) {
                VerificationToken last = optLast.get();
                if (last.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(RESEND_COOLDOWN_SECONDS))) {
                    return ApiResponse.failure("Please wait before requesting another OTP.");
                }
            }

            String otp = verificationTokenService.generateToken(user);
            emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), otp);

            return ApiResponse.success(SuccessMessages.EMAIL_SENT, null);

        } catch (IllegalArgumentException ex) {
            log.warn("Resend OTP failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Resend OTP error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }


    @Transactional
    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        String loginId = request.getEmail();

        try {
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(loginId);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByPhoneNumber(loginId);
            }

            User user = userOpt.orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            if (!user.isVerified()) {
                return ApiResponse.failure(ErrorMessages.EMAIL_NOT_VERIFIED);
            }

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginId, request.getPassword())

            );

            UUID userId = user.getId();
            String accessToken = jwtUtil.generateAccessToken(userId, user.getEmail(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(userId, user.getEmail());

            user.setLoggedIn(true);
            userRepository.save(user);

            LoginResponse response = new LoginResponse(
                    SuccessMessages.USER_LOGGED_IN,
                    accessToken,
                    refreshToken,
                    user.getRole(),
                    true,
                    user.getId().toString()
            );

            return ApiResponse.success(SuccessMessages.USER_LOGGED_IN, response);

        } catch (Exception ex) {
            log.warn("Login failed: {}", ex.getMessage());
            return ApiResponse.failure(ErrorMessages.INVALID_CREDENTIALS);
        }
    }

    @Override
    public ApiResponse<LoginResponse> refreshAccessToken(String refreshToken) {
        try {
            if (!jwtUtil.validateToken(refreshToken)) {
                return ApiResponse.failure(ErrorMessages.INVALID_TOKEN);
            }

            UUID userId = jwtUtil.extractUserId(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

            LoginResponse response = new LoginResponse(
                    SuccessMessages.TOKEN_REFRESHED,
                    newAccessToken,
                    refreshToken,
                    user.getRole(),
                    true,
                    user.getId().toString()
            );

            return ApiResponse.success(SuccessMessages.TOKEN_REFRESHED, response);

        } catch (IllegalArgumentException ex) {
            log.warn("Refresh token failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Refresh token error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    @Override
    public ApiResponse<LogoutUserResponse> logout() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ApiResponse.failure(ErrorMessages.USER_NOT_AUTHENTICATED);
            }

            UUID userId = UUID.fromString(auth.getName());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            user.setLoggedIn(false);
            userRepository.save(user);

            LogoutUserResponse logoutResponse = new LogoutUserResponse();
            logoutResponse.setLoggedIn(false);
            logoutResponse.setMessage(SuccessMessages.USER_LOGGED_OUT);

            return ApiResponse.success(SuccessMessages.USER_LOGGED_OUT, logoutResponse);

        } catch (IllegalArgumentException ex) {
            log.warn("Logout failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Logout error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }


    @Override
    public ApiResponse<String> forgotPassword(String email) {
        try {
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            String token = verificationTokenService.generateToken(user);
            String resetUrl = "https://tappr.com/reset-password?token=" + token;

            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetUrl);

            return ApiResponse.success(SuccessMessages.EMAIL_SENT, null);
        } catch (IllegalArgumentException ex) {
            log.warn("Forgot password failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Forgot password error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    @Transactional
    @Override
    public ApiResponse<String> resetPassword(String token, String newPassword) {
        try {
            VerificationStatus status = verificationTokenService.validateToken(token);

            if (status == VerificationStatus.INVALID) return ApiResponse.failure(ErrorMessages.INVALID_TOKEN);
            if (status == VerificationStatus.EXPIRED) return ApiResponse.failure(ErrorMessages.TOKEN_EXPIRED);

            VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.INVALID_TOKEN));

            User user = verificationToken.getUser();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            verificationTokenRepository.delete(verificationToken);

            return ApiResponse.success(SuccessMessages.OPERATION_SUCCESSFUL, null);
        } catch (IllegalArgumentException ex) {
            log.warn("Reset password failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Reset password error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    private void validateSignUpRequest(CreateNewUserRequest request) {
        nullOrEmptyValueChecker(request);
        validateEntriesForSpecialCharsAndWhiteSpaces(request);
    }

    private static void nullOrEmptyValueChecker(CreateNewUserRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty())
            throw new TapprException("First name is required");
        if (request.getLastName() == null || request.getLastName().trim().isEmpty())
            throw new TapprException("Last name is required");
        if (request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new TapprException("Email is required");
        if (request.getPassword() == null || request.getPassword().trim().isEmpty())
            throw new TapprException("Password is required");
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())
            throw new TapprException("Phone number is required");
    }

    private static void validateEntriesForSpecialCharsAndWhiteSpaces(CreateNewUserRequest request) {
        String emailRegex = "^[\\w+.'-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!request.getEmail().matches(emailRegex))
            throw new TapprException("Invalid email format");
        String nameRegex = "^[\\p{L}' -]+$";
        if (!request.getFirstName().matches(nameRegex))
            throw new TapprException("First name contains invalid characters");
        if (!request.getLastName().matches(nameRegex))
            throw new TapprException("Last name contains invalid characters");
        if (request.getPassword().contains(" "))
            throw new TapprException("Password must not contain whitespace");
    }
}
