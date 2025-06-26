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
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService Implementation Tests")
class WalletServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User testUser;
    private CreateWalletRequest createWalletRequest;
    private String validJwtToken;

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
    }

    @Test
    @DisplayName("Should create wallet successfully for valid user")
    public void shouldCreateWalletSuccessfully() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertNotNull(response);
        assertEquals("Wallet created successfully", response.getMessage());
        assertEquals(WalletStatus.ACTIVE, response.getWalletStatus());
        assertEquals(WalletType.FIAT, response.getWalletType());
        assertEquals(WalletCurrency.NGN, response.getWalletCurrency());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        assertEquals("1234567890", response.getAccountNumber());

        verify(jwtUtil).extractEmail(validJwtToken);
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(walletRepository).existsByUser(testUser);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should create wallet with crypto currency successfully")
    public void shouldCreateCryptoWalletSuccessfully() {
        createWalletRequest.setType(WalletType.CRYPTO);
        createWalletRequest.setCurrencyType(WalletCurrency.BTC);

        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertNotNull(response);
        assertEquals("Wallet created successfully", response.getMessage());
        assertEquals(WalletStatus.ACTIVE, response.getWalletStatus());
        assertEquals(WalletType.CRYPTO, response.getWalletType());
        assertEquals(WalletCurrency.NGN, response.getWalletCurrency());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        assertEquals("1234567890", response.getAccountNumber());
    }

    @Test
    @DisplayName("Should handle phone number longer than 10 digits correctly")
    public void shouldHandleLongPhoneNumberCorrectly() {
        testUser.setPhoneNumber("234567890123");
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertNotNull(response);
        assertEquals("4567890123", response.getAccountNumber());
    }

    @Test
    @DisplayName("Should handle phone number with exactly 10 digits")
    public void shouldHandleExact10DigitPhoneNumber() {
        testUser.setPhoneNumber("1234567890"); // Exactly 10 digits
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertNotNull(response);
        assertEquals("1234567890", response.getAccountNumber()); // Full phone number
    }

    @Test
    @DisplayName("Should handle phone number with less than 10 digits")
    public void shouldHandleShortPhoneNumber() {
        testUser.setPhoneNumber("12345"); // Less than 10 digits
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertNotNull(response);
        assertEquals("0000012345", response.getAccountNumber());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    public void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> walletService.createWalletForUser(validJwtToken, createWalletRequest)
        );

        assertEquals("User not found", exception.getMessage());
        verify(jwtUtil).extractEmail(validJwtToken);
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(walletRepository, never()).existsByUser(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when user already has a wallet")
    public void shouldThrowIllegalStateExceptionWhenUserAlreadyHasWallet() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> walletService.createWalletForUser(validJwtToken, createWalletRequest)
        );

        assertEquals("User already has a wallet", exception.getMessage());
        verify(jwtUtil).extractEmail(validJwtToken);
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(walletRepository).existsByUser(testUser);
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should verify wallet entity is properly configured before saving")
    public void shouldVerifyWalletEntityIsProperlyConfigured() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

        walletService.createWalletForUser(validJwtToken, createWalletRequest);

        verify(walletRepository).save(walletCaptor.capture());
        Wallet savedWallet = walletCaptor.getValue();

        assertNotNull(savedWallet);
        assertEquals(testUser, savedWallet.getUser());
        assertEquals(BigDecimal.ZERO, savedWallet.getBalance());
        assertEquals(WalletType.FIAT, savedWallet.getWalletType());
        assertEquals(WalletCurrency.NGN, savedWallet.getCurrencyType());
        assertEquals(WalletStatus.ACTIVE, savedWallet.getStatus());
        assertEquals("1234567890", savedWallet.getAccountNumber());
    }

    @Test
    @DisplayName("Should test all supported fiat currencies")
    public void shouldTestAllSupportedFiatCurrencies() {
        WalletCurrency[] fiatCurrencies = {WalletCurrency.NGN, WalletCurrency.USD, WalletCurrency.GBP, WalletCurrency.EUR};

        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        for (WalletCurrency currency : fiatCurrencies) {
            reset(walletRepository);
            when(walletRepository.existsByUser(testUser)).thenReturn(false);
            when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

            createWalletRequest.setType(WalletType.FIAT);
            createWalletRequest.setCurrencyType(currency);

            CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

            assertNotNull(response, "Response should not be null for currency: " + currency);
            assertEquals(WalletType.FIAT, response.getWalletType(), "Wallet type should be FIAT for: " + currency);
        }
    }

    @Test
    @DisplayName("Should test all supported crypto currencies")
    public void shouldTestAllSupportedCryptoCurrencies() {
        WalletCurrency[] cryptoCurrencies = {WalletCurrency.BTC, WalletCurrency.ETH, WalletCurrency.USDT, WalletCurrency.SUI};

        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        for (WalletCurrency currency : cryptoCurrencies) {
            reset(walletRepository);
            when(walletRepository.existsByUser(testUser)).thenReturn(false);
            when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

            createWalletRequest.setType(WalletType.CRYPTO);
            createWalletRequest.setCurrencyType(currency);

            CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

            assertNotNull(response, "Response should not be null for currency: " + currency);
            assertEquals(WalletType.CRYPTO, response.getWalletType(), "Wallet type should be CRYPTO for: " + currency);
        }
    }

    @Test
    @DisplayName("Should handle null JWT token gracefully")
    public void shouldHandleNullJwtTokenGracefully() {
        assertThrows(
                Exception.class,
                () -> walletService.createWalletForUser(null, createWalletRequest),
                "Should throw exception when JWT token is null"
        );

        verify(userRepository, never()).findByEmail(anyString());
        verify(walletRepository, never()).existsByUser(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle empty JWT token gracefully")
    public void shouldHandleEmptyJwtTokenGracefully() {
        assertThrows(
                Exception.class,
                () -> walletService.createWalletForUser("", createWalletRequest),
                "Should throw exception when JWT token is empty"
        );

        verify(userRepository, never()).findByEmail(anyString());
        verify(walletRepository, never()).existsByUser(any());
        verify(walletRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null CreateWalletRequest gracefully")
    public void shouldHandleNullCreateWalletRequestGracefully() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);

        assertThrows(
                Exception.class,
                () -> walletService.createWalletForUser(validJwtToken, null),
                "Should throw exception when CreateWalletRequest is null"
        );
    }

    @Test
    @DisplayName("Should handle repository save failure")
    public void shouldHandleRepositorySaveFailure() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> walletService.createWalletForUser(validJwtToken, createWalletRequest)
        );

        assertEquals("Database error", exception.getMessage());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should verify all dependencies are called in correct order")
    public void shouldVerifyDependenciesCalledInCorrectOrder() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        walletService.createWalletForUser(validJwtToken, createWalletRequest);

        var inOrder = inOrder(jwtUtil, userRepository, walletRepository);
        inOrder.verify(jwtUtil).extractEmail(validJwtToken);
        inOrder.verify(userRepository).findByEmail(testUser.getEmail());
        inOrder.verify(walletRepository).existsByUser(testUser);
        inOrder.verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Should verify response mapping is complete and accurate")
    public void shouldVerifyResponseMappingIsCompleteAndAccurate() {
        when(jwtUtil.extractEmail(validJwtToken)).thenReturn(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(walletRepository.existsByUser(testUser)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWalletResponse response = walletService.createWalletForUser(validJwtToken, createWalletRequest);

        assertAll("Response should have all fields properly set",
                () -> assertNotNull(response, "Response should not be null"),
                () -> assertEquals("Wallet created successfully", response.getMessage(), "Message should be set"),
                () -> assertEquals("1234567890", response.getAccountNumber(), "Account number should be set"),
                () -> assertEquals(WalletCurrency.NGN, response.getWalletCurrency(), "Wallet currency should be set"),
                () -> assertEquals(WalletStatus.ACTIVE, response.getWalletStatus(), "Wallet status should be set"),
                () -> assertEquals(WalletType.FIAT, response.getWalletType(), "Wallet type should be set"),
                () -> assertEquals(BigDecimal.ZERO, response.getBalance(), "Balance should be set to zero")
        );
    }
}