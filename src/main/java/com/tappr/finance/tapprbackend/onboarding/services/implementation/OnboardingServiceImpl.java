package com.tappr.finance.tapprbackend.onboarding.services.implementation;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.general.messages.ErrorMessages;
import com.tappr.finance.tapprbackend.general.messages.SuccessMessages;
import com.tappr.finance.tapprbackend.onboarding.dtos.requests.CreateNewUserRequest;
import com.tappr.finance.tapprbackend.onboarding.dtos.responses.CreateNewUserResponse;
import com.tappr.finance.tapprbackend.onboarding.repositories.VerificationTokenRepository;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.OnboardingService;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.VerificationTokenService;
import com.tappr.finance.tapprbackend.security.JwtUtil;
import com.tappr.finance.tapprbackend.tapprException.TapprException;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.models.VerificationToken;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import com.tappr.finance.tapprbackend.user.enums.Role;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;


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

            String emailName = (user.getFirstName() != null && !user.getFirstName().isEmpty())
                    ? user.getFirstName()
                    : "User";

            emailService.sendVerificationEmail(user.getEmail(), emailName, otp);

            CreateNewUserResponse resp = new CreateNewUserResponse();
            resp.setUserId(user.getId().toString());
            resp.setEmail(user.getEmail());
            resp.setPhoneNumber(user.getPhoneNumber());
            resp.setMessage(SuccessMessages.USER_REGISTERED);

            return ApiResponse.success(SuccessMessages.EMAIL_SENT, resp);

        } catch (TapprException | IllegalArgumentException ex) {
            log.warn("Registration validation failed: {}", ex.getMessage());
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Registration error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    @Transactional
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

        return ApiResponse.success(SuccessMessages.EMAIL_VERIFICATION_SUCCESSFUL, VerificationStatus.ACTIVE);
    }

    @Transactional
    @Override
    public ApiResponse<LoginResponse> verifyEmailAndLogin(String email, String otp) {
        try {
            ApiResponse<VerificationStatus> verificationResult = verificationTokenService.validateToken(email, otp);

            if (!verificationResult.isSuccess()) {
                return ApiResponse.failure(verificationResult.getMessage());
            }

            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.USER_NOT_FOUND));

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

            if (user.isVerified()) {
                return ApiResponse.success("User is already verified.", null);
            }

            String otp = verificationTokenService.generateToken(user);

            String emailName = (user.getFirstName() != null && !user.getFirstName().isEmpty())
                    ? user.getFirstName()
                    : "User";

            emailService.sendVerificationEmail(user.getEmail(), emailName, otp);

            return ApiResponse.success(SuccessMessages.EMAIL_SENT, null);

        } catch (TapprException ex) {
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Resend OTP error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    private void validateSignUpRequest(CreateNewUserRequest request) {
        nullOrEmptyValueChecker(request);
        validateEntriesForSpecialCharsAndWhiteSpaces(request);
    }

    private static void nullOrEmptyValueChecker(CreateNewUserRequest request) {
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

        if (request.getPassword().contains(" "))
            throw new TapprException("Password must not contain whitespace");
    }
}