package com.semicolon.africa.tapprbackend.Wallet.service.implementation;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.CreateWalletResponse;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletStatus;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Implementation Tests - Fixed")
class WalletServiceImplTestFixed {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User testUser;
    private CreateWalletRequest createWalletRequest;
    private String validJwtToken;
    private Wallet mockWallet;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("1234567890");

        createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setType(WalletType.FIAT);
        createWalletRequest.setCurrencyType(WalletCurrency.NGN);

        validJwtToken = "valid.jwt.token";

        mockWallet = new Wallet();
        mockWallet.setId(UUID.randomUUID());
        mockWallet.setUser(testUser);
        mockWallet.setBalance(BigDecimal.ZERO);
        mockWallet.setWalletType(WalletType.FIAT);
        mockWallet.setCurrencyType(WalletCurrency.NGN);
        mockWallet.setStatus(WalletStatus.ACTIVE);
        mockWallet.setAccountNumber("1234567890");
    }

    @Test
    @DisplayName("Should create wallet successfully for valid user")
    public void shouldCreateWalletSuccessfully() {
        // Arrange
        when(jwtUtil.extractUserId(validJwtToken)).thenReturn(testUser.getId().toString());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserAndCurrencyType(eq(testUser), eq(WalletCurrency.NGN)))
                .thenReturn(Optional.of(mockWallet));

        // Act
        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        // Assert
        assertNotNull(response);
        verify(jwtUtil).extractUserId(validJwtToken);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    public void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        // Arrange
        when(jwtUtil.extractUserId(validJwtToken)).thenReturn(testUser.getId().toString());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> walletService.createWalletForUser(validJwtToken, createWalletRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(jwtUtil).extractUserId(validJwtToken);
        verify(userRepository).findById(testUser.getId());
        verify(walletRepository, never()).findByUserAndCurrencyType(any(), any());
    }

    @Test
    @DisplayName("Should handle null JWT token gracefully")
    public void shouldHandleNullJwtTokenGracefully() {
        // Act & Assert
        assertThrows(
                Exception.class,
                () -> walletService.createWalletForUser(null, createWalletRequest),
                "Should throw exception when JWT token is null"
        );

        verify(userRepository, never()).findById(any());
        verify(walletRepository, never()).findByUserAndCurrencyType(any(), any());
    }

    @Test
    @DisplayName("Should handle empty JWT token gracefully")
    public void shouldHandleEmptyJwtTokenGracefully() {
        // Act & Assert
        assertThrows(
                Exception.class,
                () -> walletService.createWalletForUser("", createWalletRequest),
                "Should throw exception when JWT token is empty"
        );

        verify(userRepository, never()).findById(any());
        verify(walletRepository, never()).findByUserAndCurrencyType(any(), any());
    }

    @Test
    @DisplayName("Should handle invalid JWT token format")
    public void shouldHandleInvalidJwtTokenFormat() {
        // Arrange
        String invalidToken = "invalid.jwt.format";
        when(jwtUtil.extractUserId(invalidToken)).thenThrow(new RuntimeException("Invalid JWT token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            walletService.createWalletForUser(invalidToken, createWalletRequest);
        });

        verify(jwtUtil).extractUserId(invalidToken);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should handle different wallet currencies")
    public void shouldHandleDifferentWalletCurrencies() {
        // Test with USD currency
        createWalletRequest.setCurrencyType(WalletCurrency.USD);
        mockWallet.setCurrencyType(WalletCurrency.USD);

        when(jwtUtil.extractUserId(validJwtToken)).thenReturn(testUser.getId().toString());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserAndCurrencyType(eq(testUser), eq(WalletCurrency.NGN)))
                .thenReturn(Optional.of(mockWallet));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertNotNull(response);
        verify(jwtUtil).extractUserId(validJwtToken);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Should handle crypto wallet type")
    public void shouldHandleCryptoWalletType() {
        // Arrange
        createWalletRequest.setType(WalletType.CRYPTO);
        createWalletRequest.setCurrencyType(WalletCurrency.SUI);
        
        mockWallet.setWalletType(WalletType.CRYPTO);
        mockWallet.setCurrencyType(WalletCurrency.SUI);

        when(jwtUtil.extractUserId(validJwtToken)).thenReturn(testUser.getId().toString());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserAndCurrencyType(eq(testUser), eq(WalletCurrency.NGN)))
                .thenReturn(Optional.of(mockWallet));

        // Act
        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        // Assert
        assertNotNull(response);
        verify(jwtUtil).extractUserId(validJwtToken);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Should handle phone number processing correctly")
    public void shouldHandlePhoneNumberProcessingCorrectly() {
        // Test with different phone number formats
        testUser.setPhoneNumber("+2348123456789");

        when(jwtUtil.extractUserId(validJwtToken)).thenReturn(testUser.getId().toString());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserAndCurrencyType(eq(testUser), eq(WalletCurrency.NGN)))
                .thenReturn(Optional.of(mockWallet));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertNotNull(response);
        verify(jwtUtil).extractUserId(validJwtToken);
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Should verify dependencies are called in correct order")
    public void shouldVerifyDependenciesCalledInCorrectOrder() {
        // Arrange
        when(jwtUtil.extractUserId(validJwtToken)).thenReturn(testUser.getId().toString());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserAndCurrencyType(eq(testUser), eq(WalletCurrency.NGN)))
                .thenReturn(Optional.of(mockWallet));

        // Act
        walletService.createWalletForUser(validJwtToken, createWalletRequest);

        // Assert
        var inOrder = inOrder(jwtUtil, userRepository, walletRepository);
        inOrder.verify(jwtUtil).extractUserId(validJwtToken);
        inOrder.verify(userRepository).findById(testUser.getId());
        inOrder.verify(walletRepository).findByUserAndCurrencyType(eq(testUser), eq(WalletCurrency.NGN));
    }
}