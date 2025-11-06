package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.general.dtos.ApiResponse;
import com.semicolon.africa.tapprbackend.general.enums.VerificationStatus;
import com.semicolon.africa.tapprbackend.general.messages.ErrorMessages;
import com.semicolon.africa.tapprbackend.general.messages.SuccessMessages;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.models.VerificationToken;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.data.repositories.VerificationTokenRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.EmailService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.VerificationTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ---------------------
    // Registration & Verify
    // ---------------------

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
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            // generate OTP / verification token and send email
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

    @Transactional
    @Override
    public ApiResponse<LoginResponse> verifyEmailAndLogin(String email, String otp) {
        try {
            VerificationStatus status = verificationTokenService.validateToken(otp);

            if (status == VerificationStatus.INVALID) return ApiResponse.failure(ErrorMessages.INVALID_TOKEN);
            if (status == VerificationStatus.EXPIRED) return ApiResponse.failure(ErrorMessages.TOKEN_EXPIRED);

            VerificationToken token = verificationTokenRepository.findByTokenAndUserEmail(otp, email)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.INVALID_TOKEN));

            User user = token.getUser();
            user.setVerified(true);
            user.setLoggedIn(true);
            userRepository.save(user);

            // delete used token
            verificationTokenRepository.delete(token);

            // issue tokens
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

            // optional: send "account verified" notification
            emailService.sendEmailVerifiedEmail(user.getEmail(), user.getFirstName(), "https://tappr.africa/dashboard");

            return ApiResponse.success(SuccessMessages.USER_VERIFIED_AND_LOGGED_IN, response);

        } catch (IllegalArgumentException ex) {
            log.warn("Verify & login failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Verify & login error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    // ---------------------
    // Resend OTP
    // ---------------------

    @Transactional
    @Override
    public ApiResponse<String> resendVerificationOtp(String email) {
        try {
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            if (user.isVerified()) return ApiResponse.failure(SuccessMessages.EMAIL_VERIFIED);

            String otp = verificationTokenService.generateToken(user);
            emailService.resendVerificationEmail(user.getEmail());

            return ApiResponse.success(SuccessMessages.EMAIL_SENT, otp);

        } catch (IllegalArgumentException ex) {
            log.warn("Resend OTP failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Resend OTP error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    // ---------------------
    // Login / Refresh / Logout
    // ---------------------

    @Transactional
    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            // allow email or phone login (emailOrPhone)
            Optional<User> userOpt = userRepository.findByEmailIgnoreCase(request.getEmail());
            if (userOpt.isEmpty()) userOpt = userRepository.findByPhoneNumber(request.getEmail());

            User user = userOpt.orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            if (!user.isVerified()) return ApiResponse.failure(ErrorMessages.EMAIL_NOT_VERIFIED);

            // authenticate via AuthenticationManager (delegates to configured UserDetailsService)
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // auth.getName() should be user id string if your UserDetails implementation sets it
            UUID userId = UUID.fromString(auth.getName());
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
            // retrieving auth context should be done in controller or security utilities;
            // here we assume SecurityContextHolder available in your app (similar to QueueNI)
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
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

    // ---------------------
    // Password reset flows
    // ---------------------

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

    // =========================
    // Helper validation methods
    // =========================

    private void validateSignUpRequest(CreateNewUserRequest request) {
        nullOrEmptyValueChecker(request);
        validateEntriesForSpecialCharsAndWhiteSpaces(request);
    }

    private static void nullOrEmptyValueChecker(CreateNewUserRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty())
            throw new IllegalArgumentException("First name is required");
        if (request.getLastName() == null || request.getLastName().trim().isEmpty())
            throw new IllegalArgumentException("Last name is required");
        if (request.getEmail() == null || request.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("Email is required");
        if (request.getPassword() == null || request.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("Password is required");
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty())
            throw new IllegalArgumentException("Phone number is required");
    }

    private static void validateEntriesForSpecialCharsAndWhiteSpaces(CreateNewUserRequest request) {
        String emailRegex = "^[\\w+.'-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!request.getEmail().matches(emailRegex))
            throw new IllegalArgumentException("Invalid email format");
        String nameRegex = "^[\\p{L}' -]+$";
        if (!request.getFirstName().matches(nameRegex))
            throw new IllegalArgumentException("First name contains invalid characters");
        if (!request.getLastName().matches(nameRegex))
            throw new IllegalArgumentException("Last name contains invalid characters");
        if (request.getPassword().contains(" "))
            throw new IllegalArgumentException("Password must not contain whitespace");
    }
}
