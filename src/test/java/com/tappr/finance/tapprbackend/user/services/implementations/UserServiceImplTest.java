package com.tappr.finance.tapprbackend.user.services.implementations;

import com.tappr.finance.tapprbackend.Wallet.service.interfaces.WalletService;
import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.general.messages.ErrorMessages;
import com.tappr.finance.tapprbackend.general.messages.SuccessMessages;
import com.tappr.finance.tapprbackend.kyc.dtos.requests.IdVerificationRequest;
import com.tappr.finance.tapprbackend.kyc.dtos.responses.KycProviderResponse;
import com.tappr.finance.tapprbackend.kyc.enums.KycLevel;
import com.tappr.finance.tapprbackend.kyc.services.interfaces.KycProviderService;
import com.tappr.finance.tapprbackend.onboarding.repositories.VerificationTokenRepository;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.EmailService;
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
import com.tappr.finance.tapprbackend.user.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private VerificationTokenService verificationTokenService;
    @Mock private EmailService emailService;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private WalletService walletService;
    @Mock private KycProviderService smileIdProvider;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Set the value for @Value("${reset_password_url}")
        ReflectionTestUtils.setField(userService, "resetPasswordUrl", "http://test.com/reset?token=");

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setPhoneNumber("08012345678");
        mockUser.setPasswordHash("encodedHash");
        mockUser.setRole(Role.REGULAR);
        mockUser.setVerified(true); // Default to verified for login tests
        mockUser.setKycLevel(KycLevel.TIER_0);
    }

    // ==========================================
    // LOGIN TESTS
    // ==========================================

    @Test
    void login_ShouldReturnSuccess_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(jwtUtil.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(), any())).thenReturn("refresh-token");

        ApiResponse<LoginResponse> response = userService.login(request);

        assertTrue(response.isSuccess());
        assertEquals(SuccessMessages.USER_LOGGED_IN, response.getMessage());
        assertTrue(mockUser.isLoggedIn());
        verify(userRepository).save(mockUser);
    }

    @Test
    void login_ShouldFail_WhenUserNotVerified() {
        mockUser.setVerified(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(mockUser));

        ApiResponse<LoginResponse> response = userService.login(request);

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.EMAIL_NOT_VERIFIED, response.getMessage());
    }

    @Test
    void login_ShouldFail_WhenBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad creds"));

        ApiResponse<LoginResponse> response = userService.login(request);

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.INVALID_CREDENTIALS, response.getMessage());
    }

    // ==========================================
    // LOGOUT TESTS (Static Mocking)
    // ==========================================

    @Test
    void logout_ShouldSuccess_WhenAuthenticated() {
        setupSecurityContext();

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        ApiResponse<LogoutUserResponse> response = userService.logout();

        assertTrue(response.isSuccess());
        assertFalse(mockUser.isLoggedIn());
        verify(userRepository).save(mockUser);

        closeSecurityContext();
    }

    // ==========================================
    // PASSWORD RECOVERY TESTS
    // ==========================================

    @Test
    void forgotPassword_ShouldSuccess_WhenUserExists() {
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(mockUser));
        when(verificationTokenService.generateToken(mockUser)).thenReturn("reset-token");

        ApiResponse<String> response = userService.forgotPassword("test@example.com");

        assertTrue(response.isSuccess());
        verify(emailService).sendPasswordResetEmail(any(), any(), contains("reset-token"));
    }

    @Test
    void resetPassword_ShouldSuccess_WhenTokenValid() {
        VerificationToken token = new VerificationToken();
        token.setUser(mockUser);

        when(verificationTokenService.validateToken("valid-token")).thenReturn(VerificationStatus.ACTIVE);
        when(verificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");

        ApiResponse<String> response = userService.resetPassword("valid-token", "newPass");

        assertTrue(response.isSuccess());
        assertEquals("newEncoded", mockUser.getPasswordHash());
        verify(verificationTokenRepository).delete(token);
    }

    @Test
    void resetPassword_ShouldFail_WhenTokenExpired() {
        when(verificationTokenService.validateToken("expired")).thenReturn(VerificationStatus.EXPIRED);

        ApiResponse<String> response = userService.resetPassword("expired", "newPass");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.TOKEN_EXPIRED, response.getMessage());
    }

    // ==========================================
    // RESEND OTP TESTS
    // ==========================================

    @Test
    void resendVerificationOtp_ShouldFail_WhenCooldownActive() {
        mockUser.setVerified(false);
        VerificationToken lastToken = new VerificationToken();
        lastToken.setCreatedAt(LocalDateTime.now().minusSeconds(10)); // Created 10s ago (Cooldown is 60s)

        when(userRepository.findByEmailIgnoreCase(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(verificationTokenRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(lastToken));

        ApiResponse<String> response = userService.resendVerificationOtp(mockUser.getEmail());

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("wait before requesting"));
    }

    @Test
    void resendVerificationOtp_ShouldSuccess_WhenCooldownPassed() {
        mockUser.setVerified(false);
        VerificationToken lastToken = new VerificationToken();
        lastToken.setCreatedAt(LocalDateTime.now().minusSeconds(61)); // Created 61s ago

        when(userRepository.findByEmailIgnoreCase(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(verificationTokenRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(lastToken));
        when(verificationTokenService.generateToken(mockUser)).thenReturn("new-otp");

        ApiResponse<String> response = userService.resendVerificationOtp(mockUser.getEmail());

        assertTrue(response.isSuccess());
        verify(emailService).sendVerificationEmail(any(), any(), any());
    }

    // ==========================================
    // PROFILE SETUP TESTS
    // ==========================================

    @Test
    void setupProfile_ShouldSuccess_WhenUsernameAvailable() {
        setupSecurityContext();
        ProfileSetupRequest request = new ProfileSetupRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setUsername(Optional.of("johndoe"));

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(userRepository.findUserByUsername("johndoe")).thenReturn(Optional.empty()); // Available

        ApiResponse<ProfileSetupResponse> response = userService.setupProfile(request);

        assertTrue(response.isSuccess());
        assertEquals("John", mockUser.getFirstName());
        assertEquals("johndoe", mockUser.getUsername());
        assertTrue(mockUser.isProfileSetupComplete());

        closeSecurityContext();
    }

    @Test
    void setupProfile_ShouldFail_WhenUsernameTakenByOther() {
        setupSecurityContext();
        ProfileSetupRequest request = new ProfileSetupRequest();
        request.setUsername(Optional.of("taken"));

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID()); // Different ID

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(userRepository.findUserByUsername("taken")).thenReturn(Optional.of(otherUser));

        ApiResponse<ProfileSetupResponse> response = userService.setupProfile(request);

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.USERNAME_NOT_AVAILABLE, response.getMessage());

        closeSecurityContext();
    }

    // ==========================================
    // KYC TIER 1 TESTS
    // ==========================================

    @Test
    void startKycTier1_ShouldSuccess_WhenProviderApproves() {
        setupSecurityContext();
        IdVerificationRequest kycRequest = new IdVerificationRequest();

        KycProviderResponse providerResponse = new KycProviderResponse();
        providerResponse.setSuccess(true);

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(smileIdProvider.submitVerification(mockUser.getId(), kycRequest)).thenReturn(providerResponse);

        ApiResponse<KycProviderResponse> response = userService.startKycTier1(kycRequest);

        assertTrue(response.isSuccess());
        assertEquals(SuccessMessages.KYC_VERIFICATION_SUCCESSFUL, response.getMessage());

        assertTrue(mockUser.isKycVerified());
        assertEquals(KycLevel.TIER_1, mockUser.getKycLevel());
        verify(walletService).createWalletForUser(mockUser);

        closeSecurityContext();
    }

    @Test
    void startKycTier1_ShouldReturnSuccess_WhenAlreadyVerified() {
        setupSecurityContext();
        mockUser.setKycLevel(KycLevel.TIER_1);
        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        ApiResponse<KycProviderResponse> response = userService.startKycTier1(new IdVerificationRequest());

        assertTrue(response.isSuccess());
        assertEquals("Already Verified", response.getMessage());
        verify(smileIdProvider, never()).submitVerification(any(), any());

        closeSecurityContext();
    }

    @Test
    void startKycTier1_ShouldFail_WhenProviderRejects() {
        setupSecurityContext();
        IdVerificationRequest kycRequest = new IdVerificationRequest();

        KycProviderResponse providerResponse = new KycProviderResponse();
        providerResponse.setSuccess(false);
        providerResponse.setMessage("Face mismatch");

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));
        when(smileIdProvider.submitVerification(mockUser.getId(), kycRequest)).thenReturn(providerResponse);

        ApiResponse<KycProviderResponse> response = userService.startKycTier1(kycRequest);

        assertFalse(response.isSuccess());
        assertEquals("Face mismatch", response.getMessage());
        verify(walletService, never()).createWalletForUser(any());

        closeSecurityContext();
    }

    // --- HELPER FOR STATIC MOCKING ---
    private void setupSecurityContext() {
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(mockUser.getId().toString());
    }

    private void closeSecurityContext() {
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
        }
    }
}