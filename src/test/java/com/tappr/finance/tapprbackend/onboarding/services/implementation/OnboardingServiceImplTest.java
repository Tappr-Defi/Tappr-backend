package com.tappr.finance.tapprbackend.onboarding.services.implementation;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.general.messages.ErrorMessages;
import com.tappr.finance.tapprbackend.general.messages.SuccessMessages;
import com.tappr.finance.tapprbackend.onboarding.dtos.requests.CreateNewUserRequest;
import com.tappr.finance.tapprbackend.onboarding.dtos.responses.CreateNewUserResponse;
import com.tappr.finance.tapprbackend.onboarding.repositories.VerificationTokenRepository;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.EmailService;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.VerificationTokenService;
import com.tappr.finance.tapprbackend.security.JwtUtil;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.models.VerificationToken;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import com.tappr.finance.tapprbackend.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    private CreateNewUserRequest signUpRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        signUpRequest = new CreateNewUserRequest();
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPhoneNumber("08012345678");
        signUpRequest.setPassword("securePassword");

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("TestUser");
        mockUser.setRole(Role.REGULAR);
        mockUser.setVerified(false);
    }

    @Test
    void register_ShouldReturnSuccess_WhenRequestIsValid() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(verificationTokenService.generateToken(any(User.class))).thenReturn("123456");

        ApiResponse<CreateNewUserResponse> response = onboardingService.register(signUpRequest);

        assertTrue(response.isSuccess());
        assertEquals(SuccessMessages.EMAIL_SENT, response.getMessage());
        assertNotNull(response.getData());
        assertEquals("test@example.com", response.getData().getEmail());

        verify(emailService, times(1)).sendVerificationEmail(eq("test@example.com"), any(), eq("123456"));
    }

    @Test
    void register_ShouldFail_WhenEmailAlreadyExists() {
        when(userRepository.findByEmailIgnoreCase(signUpRequest.getEmail())).thenReturn(Optional.of(mockUser));

        ApiResponse<CreateNewUserResponse> response = onboardingService.register(signUpRequest);

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.EMAIL_ALREADY_EXISTS, response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldFail_WhenPhoneNumberAlreadyExists() {
        when(userRepository.findByEmailIgnoreCase(signUpRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(signUpRequest.getPhoneNumber())).thenReturn(Optional.of(mockUser));

        ApiResponse<CreateNewUserResponse> response = onboardingService.register(signUpRequest);

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.PHONE_ALREADY_EXISTS, response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldFail_WhenInputIsInvalid() {
        signUpRequest.setEmail("invalid-email");

        ApiResponse<CreateNewUserResponse> response = onboardingService.register(signUpRequest);

        assertFalse(response.isSuccess());
        assertEquals("Invalid email format", response.getMessage());
    }


    @Test
    void validateToken_ShouldReturnSuccess_WhenTokenIsValid() {
        String tokenString = "123456";
        VerificationToken token = new VerificationToken();
        token.setToken(tokenString);
        token.setUser(mockUser);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);

        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(mockUser.getEmail(), tokenString))
                .thenReturn(Optional.of(token));

        ApiResponse<VerificationStatus> response = onboardingService.validateToken(mockUser.getEmail(), tokenString);

        assertTrue(response.isSuccess());
        assertEquals(VerificationStatus.ACTIVE, response.getData());
        assertTrue(token.isUsed());
        assertTrue(mockUser.isVerified());

        verify(verificationTokenRepository, times(1)).save(token);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void validateToken_ShouldFail_WhenTokenNotFound() {
        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(anyString(), anyString()))
                .thenReturn(Optional.empty());

        ApiResponse<VerificationStatus> response = onboardingService.validateToken("email", "otp");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.INVALID_TOKEN, response.getMessage());
        assertEquals(VerificationStatus.INVALID, response.getData());
    }

    @Test
    void validateToken_ShouldFail_WhenTokenExpired() {
        VerificationToken token = new VerificationToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(5)); // Expired

        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(anyString(), anyString()))
                .thenReturn(Optional.of(token));

        ApiResponse<VerificationStatus> response = onboardingService.validateToken("email", "otp");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.TOKEN_EXPIRED, response.getMessage());
        assertEquals(VerificationStatus.EXPIRED, response.getData());
    }

    @Test
    void validateToken_ShouldFail_WhenTokenAlreadyUsed() {
        VerificationToken token = new VerificationToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setUsed(true);

        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(anyString(), anyString()))
                .thenReturn(Optional.of(token));

        ApiResponse<VerificationStatus> response = onboardingService.validateToken("email", "otp");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.INVALID_TOKEN, response.getMessage());
        assertEquals(VerificationStatus.INVALID, response.getData());
    }

    @Test
    void verifyEmailAndLogin_ShouldReturnSuccess_WhenServiceValidates() {
        String otp = "123456";
        when(verificationTokenService.validateToken(mockUser.getEmail(), otp))
                .thenReturn(ApiResponse.success("Valid", VerificationStatus.ACTIVE));

        when(userRepository.findByEmailIgnoreCase(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        ApiResponse<LoginResponse> response = onboardingService.verifyEmailAndLogin(mockUser.getEmail(), otp);

        assertTrue(response.isSuccess());
        assertEquals(SuccessMessages.USER_VERIFIED_AND_LOGGED_IN, response.getMessage());
        assertNotNull(response.getData().getAccessToken());
        assertTrue(mockUser.isLoggedIn());
        verify(userRepository).save(mockUser);
    }

    @Test
    void verifyEmailAndLogin_ShouldFail_WhenTokenServiceFails() {
        when(verificationTokenService.validateToken(anyString(), anyString()))
                .thenReturn(ApiResponse.failure("Invalid Token"));

        ApiResponse<LoginResponse> response = onboardingService.verifyEmailAndLogin("email", "otp");

        assertFalse(response.isSuccess());
        assertEquals("Invalid Token", response.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void resendVerificationOtp_ShouldSuccess_WhenUserExistsAndUnverified() {
        mockUser.setVerified(false);
        when(userRepository.findByEmailIgnoreCase(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(verificationTokenService.generateToken(mockUser)).thenReturn("654321");

        ApiResponse<String> response = onboardingService.resendVerificationOtp(mockUser.getEmail());

        assertTrue(response.isSuccess());
        assertEquals(SuccessMessages.EMAIL_SENT, response.getMessage());
        verify(emailService).sendVerificationEmail(eq(mockUser.getEmail()), any(), eq("654321"));
    }

    @Test
    void resendVerificationOtp_ShouldReturnInfo_WhenUserAlreadyVerified() {
        mockUser.setVerified(true);
        when(userRepository.findByEmailIgnoreCase(mockUser.getEmail())).thenReturn(Optional.of(mockUser));

        ApiResponse<String> response = onboardingService.resendVerificationOtp(mockUser.getEmail());

        assertTrue(response.isSuccess());
        assertEquals("User is already verified.", response.getMessage());
        verify(verificationTokenService, never()).generateToken(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void resendVerificationOtp_ShouldFail_WhenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        ApiResponse<String> response = onboardingService.resendVerificationOtp("unknown@email.com");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.USER_NOT_FOUND, response.getMessage());
    }
}