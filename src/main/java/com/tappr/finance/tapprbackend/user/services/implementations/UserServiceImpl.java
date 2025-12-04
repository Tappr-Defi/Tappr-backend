package com.tappr.finance.tapprbackend.user.services.implementations;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.general.messages.ErrorMessages;
import com.tappr.finance.tapprbackend.general.messages.SuccessMessages;
import com.tappr.finance.tapprbackend.onboarding.repositories.VerificationTokenRepository;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.VerificationTokenService;
import com.tappr.finance.tapprbackend.security.JwtUtil;
import com.tappr.finance.tapprbackend.tapprException.TapprException;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.models.VerificationToken;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import com.tappr.finance.tapprbackend.user.dtos.requests.LoginRequest;
import com.tappr.finance.tapprbackend.user.dtos.requests.ProfileSetupRequest;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.ProfileSetupResponse;
import com.tappr.finance.tapprbackend.user.services.interfaces.UserService;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.EmailService;
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
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final VerificationTokenRepository verificationTokenRepository;

    private static final int RESEND_COOLDOWN_SECONDS = 60;



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
    public ApiResponse<String> resendVerificationOtp(String email) {
        try {
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new TapprException(ErrorMessages.USER_NOT_FOUND));

            if (user.isVerified()) return ApiResponse.failure(SuccessMessages.USER_ALREADY_VERIFIED);

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
    public ApiResponse<String> resetPassword(String token, String newPassword) {
        try {
            VerificationStatus status = verificationTokenService.validateToken(token);

            if (status == VerificationStatus.INVALID) return ApiResponse.failure(ErrorMessages.INVALID_TOKEN);
            if (status == VerificationStatus.EXPIRED) return ApiResponse.failure(ErrorMessages.TOKEN_EXPIRED);

            // The token is valid; proceed with password update
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.INVALID_TOKEN));

            User user = verificationToken.getUser();
            // The service logic to update the password hash
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Delete the token immediately after use
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

    @Override
    public ApiResponse<ProfileSetupResponse> setupProfile(ProfileSetupRequest request) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ApiResponse.failure(ErrorMessages.USER_NOT_AUTHENTICATED);
            }

            UUID userId = UUID.fromString(auth.getName());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            Optional<String> uniqueUsername = request.getUsername();
            User foundUserByUsername = (User) userRepository.findUserByUsername(String.valueOf(uniqueUsername));

            if (foundUserByUsername != null) throw new TapprException(ErrorMessages.USERNAME_NOT_AVAILABLE);

            user.setUsername(uniqueUsername.toString());
            userRepository.save(user);

            ProfileSetupResponse profileSetupResponse = new ProfileSetupResponse();
            profileSetupResponse.setMessage(SuccessMessages.PROFILE_UPDATE_SUCCESSFUL);


            return ApiResponse.success(SuccessMessages.PROFILE_UPDATE_SUCCESSFUL, profileSetupResponse);

        } catch (IllegalArgumentException ex) {
            log.warn("Logout failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Logout error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }
}
