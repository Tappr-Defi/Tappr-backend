package com.tappr.finance.tapprbackend.onboarding.services.implementation;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.general.messages.ErrorMessages;
import com.tappr.finance.tapprbackend.general.messages.SuccessMessages;
import com.tappr.finance.tapprbackend.onboarding.repositories.VerificationTokenRepository;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.models.VerificationToken;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceImplTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationTokenServiceImpl verificationTokenService;

    private User mockUser;
    private VerificationToken mockToken;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setVerified(false);

        mockToken = new VerificationToken();
        mockToken.setId(UUID.randomUUID());
        mockToken.setToken("123456");
        mockToken.setUser(mockUser);
        mockToken.setUsed(false);
        mockToken.setCreatedAt(LocalDateTime.now());
        mockToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
    }


    @Test
    void generateToken_ShouldSuccess_WhenUserIsValid() {
        String token = verificationTokenService.generateToken(mockUser);

        assertNotNull(token);
        assertEquals(6, token.length());

        verify(verificationTokenRepository).deleteAllByUserId(mockUser.getId());

        verify(verificationTokenRepository).save(any(VerificationToken.class));
    }

    @Test
    void generateToken_ShouldThrow_WhenUserIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationTokenService.generateToken(null);
        });

        assertEquals("User cannot be null or must have a valid ID", exception.getMessage());
        verify(verificationTokenRepository, never()).save(any());
    }

    @Test
    void generateToken_ShouldThrow_WhenUserIdIsNull() {
        mockUser.setId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            verificationTokenService.generateToken(mockUser);
        });

        assertEquals("User cannot be null or must have a valid ID", exception.getMessage());
        verify(verificationTokenRepository, never()).save(any());
    }


    @Test
    void validateToken_EmailOtp_ShouldSuccess_WhenValid() {
        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken("test@example.com", "123456"))
                .thenReturn(Optional.of(mockToken));

        ApiResponse<VerificationStatus> response = verificationTokenService.validateToken("test@example.com", "123456");

        assertTrue(response.isSuccess());
        assertEquals(SuccessMessages.EMAIL_VERIFICATION_SUCCESSFUL, response.getMessage());
        assertEquals(VerificationStatus.ACTIVE, response.getData());

        assertTrue(mockToken.isUsed());
        assertTrue(mockUser.isVerified());
        verify(verificationTokenRepository).save(mockToken);
        verify(userRepository).save(mockUser);
    }

    @Test
    void validateToken_EmailOtp_ShouldFail_WhenNotFound() {
        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(anyString(), anyString()))
                .thenReturn(Optional.empty());

        ApiResponse<VerificationStatus> response = verificationTokenService.validateToken("wrong@email.com", "000000");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.INVALID_TOKEN, response.getMessage());
        assertEquals(VerificationStatus.INVALID, response.getData());
        verify(userRepository, never()).save(any());
    }

    @Test
    void validateToken_EmailOtp_ShouldFail_WhenExpired() {
        mockToken.setExpiresAt(LocalDateTime.now().minusMinutes(5)); // Set to past
        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(anyString(), anyString()))
                .thenReturn(Optional.of(mockToken));

        ApiResponse<VerificationStatus> response = verificationTokenService.validateToken("test@example.com", "123456");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.TOKEN_EXPIRED, response.getMessage());
        assertEquals(VerificationStatus.EXPIRED, response.getData());
        verify(userRepository, never()).save(any());
    }

    @Test
    void validateToken_EmailOtp_ShouldFail_WhenAlreadyUsed() {
        mockToken.setUsed(true);
        when(verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(anyString(), anyString()))
                .thenReturn(Optional.of(mockToken));

        ApiResponse<VerificationStatus> response = verificationTokenService.validateToken("test@example.com", "123456");

        assertFalse(response.isSuccess());
        assertEquals(ErrorMessages.INVALID_TOKEN, response.getMessage());
        assertEquals(VerificationStatus.INVALID, response.getData());
        verify(userRepository, never()).save(any());
    }

    @Test
    void validateToken_StringOnly_ShouldReturnActive_WhenValid() {
        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(mockToken));

        VerificationStatus status = verificationTokenService.validateToken("123456");

        assertEquals(VerificationStatus.ACTIVE, status);
    }

    @Test
    void validateToken_StringOnly_ShouldReturnExpired_WhenExpired() {
        mockToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(verificationTokenRepository.findByToken("123456")).thenReturn(Optional.of(mockToken));

        VerificationStatus status = verificationTokenService.validateToken("123456");

        assertEquals(VerificationStatus.EXPIRED, status);
    }

    @Test
    void validateToken_StringOnly_ShouldReturnInvalid_WhenNotFound() {
        when(verificationTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        VerificationStatus status = verificationTokenService.validateToken("invalid");

        assertEquals(VerificationStatus.INVALID, status);
    }
}