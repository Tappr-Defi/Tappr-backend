package com.tappr.finance.tapprbackend.Wallet.service.impl;

import com.tappr.finance.tapprbackend.Wallet.data.model.Currency;
import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.Wallet.data.repositories.CurrencyRepository;
import com.tappr.finance.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.tappr.finance.tapprbackend.user.data.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class
WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User mockUser;
    private Currency ngnCurrency;
    private Currency suiCurrency;

    @BeforeEach
    void setUp() {
        // Inject the @Value property
        ReflectionTestUtils.setField(walletService, "NODE_SERVICE_URL", "http://localhost:3000");

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");

        ngnCurrency = new Currency();
        ngnCurrency.setCode("NGN");
        ngnCurrency.setName("Naira");

        suiCurrency = new Currency();
        suiCurrency.setCode("SUI");
        suiCurrency.setName("Sui Token");
    }

    // ==========================================
    // CREATE WALLET (Generic) TESTS
    // ==========================================

    @Test
    void createWallet_ShouldCreateNGNWallet_WhenCurrencyIsNGN() {
        // Arrange
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "NGN")).thenReturn(Optional.empty());
        when(currencyRepository.findById("NGN")).thenReturn(Optional.of(ngnCurrency));
        when(walletRepository.existsByAccountNumber(anyString())).thenReturn(false); // Valid NUBAN check

        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Wallet wallet = walletService.createWallet(mockUser, "NGN");

        // Assert
        assertNotNull(wallet);
        assertEquals("NGN", wallet.getCurrency().getCode());
        assertNotNull(wallet.getAccountNumber());
        assertEquals(10, wallet.getAccountNumber().length());
        assertEquals(BigDecimal.ZERO, wallet.getBalance());

        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_ShouldCreateSUIWallet_WhenCurrencyIsSUI() {
        // Arrange
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "SUI")).thenReturn(Optional.empty());
        when(currencyRepository.findById("SUI")).thenReturn(Optional.of(suiCurrency));
        when(walletRepository.getNextDerivationIndex()).thenReturn(101L);

        // Mock Node.js response
        Map<String, Object> nodeResponse = Map.of("address", "0xABC123");
        when(restTemplate.postForEntity(anyString(), anyMap(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(nodeResponse));

        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Wallet wallet = walletService.createWallet(mockUser, "SUI");

        // Assert
        assertNotNull(wallet);
        assertEquals("SUI", wallet.getCurrency().getCode());
        assertEquals("0xABC123", wallet.getDepositAddress());
        assertEquals(101L, wallet.getDerivationIndex());

        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_ShouldThrowException_WhenWalletAlreadyExists() {
        // Arrange
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "NGN")).thenReturn(Optional.of(new Wallet()));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                walletService.createWallet(mockUser, "NGN")
        );
        assertTrue(ex.getMessage().contains("Wallet already exists"));

        verify(walletRepository, never()).save(any());
    }

    @Test
    void createWallet_ShouldThrowException_WhenCurrencyNotSupported() {
        // Arrange
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "BTC")).thenReturn(Optional.empty());
        when(currencyRepository.findById("BTC")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                walletService.createWallet(mockUser, "BTC")
        );
        assertTrue(ex.getMessage().contains("Currency not supported"));
    }

    @Test
    void createWallet_ShouldThrowException_WhenNodeServiceFailsForSUI() {
        // Arrange
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "SUI")).thenReturn(Optional.empty());
        when(currencyRepository.findById("SUI")).thenReturn(Optional.of(suiCurrency));
        when(walletRepository.getNextDerivationIndex()).thenReturn(101L);

        // Simulate Node Service Failure (Exception)
        when(restTemplate.postForEntity(anyString(), anyMap(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection Refused"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                walletService.createWallet(mockUser, "SUI")
        );

        // It wraps the specific error in "Key Management Service Unavailable"
        assertTrue(ex.getMessage().contains("Key Management Service Unavailable"));
    }

    // ==========================================
    // CREATE WALLET FOR USER (Orchestrator) TESTS
    // ==========================================

    @Test
    void createWalletForUser_ShouldCreateBoth_WhenUserHasNone() {
        // Arrange
        // NGN checks
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "NGN")).thenReturn(Optional.empty());
        when(currencyRepository.findById("NGN")).thenReturn(Optional.of(ngnCurrency));

        // SUI checks
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "SUI")).thenReturn(Optional.empty());
        when(currencyRepository.findById("SUI")).thenReturn(Optional.of(suiCurrency));

        // SUI Node Mock
        when(walletRepository.getNextDerivationIndex()).thenReturn(1L);
        when(restTemplate.postForEntity(anyString(), anyMap(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("address", "0x123")));

        // Act
        walletService.createWalletForUser(mockUser);

        // Assert
        verify(walletRepository, times(2)).save(any(Wallet.class)); // 1 for NGN, 1 for SUI
    }

    @Test
    void createWalletForUser_ShouldCreateOnlyMissing_WhenUserHasNGN() {
        // Arrange
        // NGN Exists
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "NGN")).thenReturn(Optional.of(new Wallet()));

        // SUI Missing
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "SUI")).thenReturn(Optional.empty());
        when(currencyRepository.findById("SUI")).thenReturn(Optional.of(suiCurrency));

        // SUI Node Mock
        when(walletRepository.getNextDerivationIndex()).thenReturn(2L);
        when(restTemplate.postForEntity(anyString(), anyMap(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("address", "0x456")));

        // Act
        walletService.createWalletForUser(mockUser);

        // Assert
        verify(walletRepository, times(1)).save(any(Wallet.class)); // Only SUI saved
    }

    @Test
    void createWalletForUser_ShouldNotFail_IfOneCreationErrors() {
        // Arrange
        // NGN Missing -> Throws Error
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "NGN")).thenReturn(Optional.empty());
        when(currencyRepository.findById("NGN")).thenThrow(new RuntimeException("DB Error"));

        // SUI Missing -> Success
        when(walletRepository.findByUserAndCurrencyCode(mockUser, "SUI")).thenReturn(Optional.empty());
        when(currencyRepository.findById("SUI")).thenReturn(Optional.of(suiCurrency));
        when(walletRepository.getNextDerivationIndex()).thenReturn(5L);
        when(restTemplate.postForEntity(anyString(), anyMap(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("address", "0x789")));

        // Act
        // This should NOT throw an exception because of the try-catch inside the method
        walletService.createWalletForUser(mockUser);

        // Assert
        // We expect SUI to still be saved even if NGN failed
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }
}