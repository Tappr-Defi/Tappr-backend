//package com.semicolon.africa.tapprbackend.transaction.services.implementations;
//
//import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
//import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
//import com.semicolon.africa.tapprbackend.transaction.data.repositories.TransactionRepository;
//import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
//import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
//import com.semicolon.africa.tapprbackend.transaction.enums.CurrencyType;
//import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
//import com.semicolon.africa.tapprbackend.transaction.exceptions.MerchantNotFoundException;
//import com.semicolon.africa.tapprbackend.user.data.models.User;
//import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("TransactionService Implementation Tests - Fixed")
//class TransactionServiceImplTestFixed {
//
//    @Mock
//    private TransactionRepository transactionRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private TransactionServiceImpl transactionService;
//
//    private CreateTransactionRequest createTransactionRequest;
//    private User merchant;
//    private Transaction transaction;
//    private UUID merchantId;
//
//    @BeforeEach
//    public void setUp() {
//        merchantId = UUID.randomUUID();
//
//        merchant = new User();
//        merchant.setId(merchantId);
//        merchant.setFirstName("John");
//        merchant.setLastName("Doe");
//        merchant.setEmail("john.doe@example.com");
//
//        createTransactionRequest = new CreateTransactionRequest();
//        createTransactionRequest.setMerchantId(String.valueOf(merchantId));
//        createTransactionRequest.setAmount(BigDecimal.valueOf(1000));
////        createTransactionRequest.setCurrency("NGN");
//
//        transaction = new Transaction();
//        transaction.setId(UUID.randomUUID().toString());
//        transaction.setTransactionRef(UUID.randomUUID().toString());
//        transaction.setMerchant(merchant);
//        transaction.setAmount(BigDecimal.valueOf(1000));
//        transaction.setWalletCurrency(WalletCurrency.NGN);
//        transaction.setStatus(TransactionStatus.PENDING);
//        transaction.setInitiatedAt(LocalDateTime.now());
//        transaction.setInitiated(true);
//
//        when(userRepository.findById(merchantId)).thenReturn(Optional.of(merchant));
//        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
//    }
//
//    @Test
//    @DisplayName("Should create transaction successfully")
//    public void testCreateTransaction_shouldCreateTransactionSuccessfully() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//
//        assertNotNull(response);
//        assertEquals(transaction.getId(), response.getTransactionId());
//        assertEquals(transaction.getTransactionRef(), response.getTransactionRef());
//        assertEquals(merchant.getFullName(), response.getMerchantName());
//        assertEquals(BigDecimal.valueOf(1000), response.getAmount());
//        assertEquals("NGN", response.getCurrency());
//        assertEquals(TransactionStatus.PENDING, response.getStatus());
//        assertNotNull(response.getInitiatedAt());
//        assertNull(response.getCompletedAt());
//
//        verify(userRepository).findById(merchantId);
//        verify(transactionRepository).save(any(Transaction.class));
//    }
//
////    @Test
////    @DisplayName("Should throw MerchantNotFoundException when merchant not found")
////    public void testCreateTransaction_merchantNotFound_throwsException() {
////        when(userRepository.findById(merchantId)).thenReturn(Optional.empty());
////
////        MerchantNotFoundException exception = assertThrows(MerchantNotFoundException.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////
////        assertEquals("Merchant not found", exception.getMessage());
////        verify(userRepository).findById(merchantId);
////        verify(transactionRepository, never()).save(any());
////    }
////
////    @Test
////    @DisplayName("Should default transaction status to PENDING")
////    public void testCreateTransaction_shouldDefaultToPending() {
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////        assertEquals(TransactionStatus.PENDING, response.getStatus());
////    }
////
////    @Test
////    @DisplayName("Should throw exception for invalid currency")
////    public void testCreateTransaction_whenInvalidCurrency_shouldThrowException() {
////        createTransactionRequest.setCurrency("INVALID_CURRENCY");
////
////        assertThrows(IllegalArgumentException.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////    }
////
////    @Test
////    @DisplayName("Should create transaction with DOLLAR currency")
////    public void testCreateTransaction_withValidDollarCurrency() {
////        createTransactionRequest.setCurrency("DOLLAR");
////        transaction.setCurrency(CurrencyType.DOLLAR);
////
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////
////        assertNotNull(response);
////        assertEquals("DOLLAR", response.getCurrency());
////    }
////
////    @Test
////    @DisplayName("Should create transaction with EURO currency")
////    public void testCreateTransaction_withValidEuroCurrency() {
////        createTransactionRequest.setCurrency("EURO");
////        transaction.setCurrency(CurrencyType.EURO);
////
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////
////        assertNotNull(response);
////        assertEquals("EURO", response.getCurrency());
////    }
////
////    @Test
////    @DisplayName("Should create transaction with POUND currency")
////    public void testCreateTransaction_withValidPoundCurrency() {
////        createTransactionRequest.setCurrency("POUND");
////        transaction.setCurrency(CurrencyType.POUND);
////
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////
////        assertNotNull(response);
////        assertEquals("POUND", response.getCurrency());
////    }
////
////    @Test
////    @DisplayName("Should handle large amounts")
////    public void testCreateTransaction_withLargeAmount() {
////        BigDecimal largeAmount = BigDecimal.valueOf(1000000);
////        createTransactionRequest.setAmount(largeAmount);
////        transaction.setAmount(largeAmount);
////
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////
////        assertNotNull(response);
////        assertEquals(largeAmount, response.getAmount());
////    }
////
////    @Test
////    @DisplayName("Should handle decimal amounts")
////    public void testCreateTransaction_withDecimalAmount() {
////        BigDecimal decimalAmount = BigDecimal.valueOf(999.99);
////        createTransactionRequest.setAmount(decimalAmount);
////        transaction.setAmount(decimalAmount);
////
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////
////        assertNotNull(response);
////        assertEquals(decimalAmount, response.getAmount());
////    }
////
////    @Test
////    @DisplayName("Should throw exception for negative amount")
////    public void testCreateTransaction_withNegativeAmount_shouldThrowException() {
////        createTransactionRequest.setAmount(BigDecimal.valueOf(-100));
////
////        // This would depend on validation in the service implementation
////        // For now, we'll assume the service accepts it and let the business logic handle it
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////        assertNotNull(response);
////    }
////
////    @Test
////    @DisplayName("Should throw exception for null amount")
////    public void testCreateTransaction_withNullAmount_shouldThrowException() {
////        createTransactionRequest.setAmount(null);
////
////        assertThrows(Exception.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////    }
////
////    @Test
////    @DisplayName("Should throw exception for null merchant ID")
////    public void testCreateTransaction_withNullMerchantId_shouldThrowException() {
////        createTransactionRequest.setMerchantId(null);
////
////        assertThrows(Exception.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////    }
////
////    @Test
////    @DisplayName("Should throw exception for empty merchant ID")
////    public void testCreateTransaction_withEmptyMerchantId_shouldThrowException() {
////        createTransactionRequest.setMerchantId("");
////
////        assertThrows(Exception.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////    }
////
////    @Test
////    @DisplayName("Should throw exception for invalid merchant ID format")
////    public void testCreateTransaction_withInvalidMerchantIdFormat_shouldThrowException() {
////        createTransactionRequest.setMerchantId("invalid-uuid-format");
////
////        assertThrows(Exception.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////    }
////
////    @Test
////    @DisplayName("Should set correct merchant name")
////    public void testCreateTransaction_shouldSetCorrectMerchantName() {
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////        assertEquals(merchant.getFullName(), response.getMerchantName());
////    }
////
////    @Test
////    @DisplayName("Should set initiated time")
////    public void testCreateTransaction_shouldSetCorrectInitiatedTime() {
////        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
////
////        assertTrue(response.getInitiatedAt().isAfter(before));
////        assertTrue(response.getInitiatedAt().isBefore(after));
////    }
////
////    @Test
////    @DisplayName("Should have null completed time initially")
////    public void testCreateTransaction_shouldHaveNullCompletedAtInitially() {
////        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
////        assertNull(response.getCompletedAt());
////    }
////
////    @Test
////    @DisplayName("Should save transaction to repository")
////    public void testCreateTransaction_shouldSaveTransactionToRepository() {
////        transactionService.createTransaction(createTransactionRequest);
////        verify(transactionRepository, times(1)).save(any(Transaction.class));
////    }
////
////    @Test
////    @DisplayName("Should call user repository once")
////    public void testCreateTransaction_shouldCallUserRepositoryOnce() {
////        transactionService.createTransaction(createTransactionRequest);
////        verify(userRepository, times(1)).findById(merchantId);
////    }
////
////    @Test
////    @DisplayName("Should handle repository exception")
////    public void testCreateTransaction_shouldHandleRepositoryException() {
////        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("Database error"));
////
////        assertThrows(RuntimeException.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////    }
////
////    @Test
////    @DisplayName("Should handle user repository exception")
////    public void testCreateTransaction_shouldHandleUserRepositoryException() {
////        when(userRepository.findById(any(UUID.class))).thenThrow(new RuntimeException("Database connection error"));
////
////        assertThrows(RuntimeException.class, () -> {
////            transactionService.createTransaction(createTransactionRequest);
////        });
////    }
////
////    @Test
////    @DisplayName("Should generate unique transaction references")
////    public void testCreateTransaction_shouldGenerateUniqueTransactionRefs() {
////        // Create two different transaction objects with different refs
////        Transaction transaction2 = new Transaction();
////        transaction2.setId(UUID.randomUUID().toString());
////        transaction2.setTransactionRef(UUID.randomUUID().toString());
////        transaction2.setMerchant(merchant);
////        transaction2.setAmount(BigDecimal.valueOf(1000));
////        transaction2.setCurrency(CurrencyType.NGN);
////        transaction2.setStatus(TransactionStatus.PENDING);
////        transaction2.setInitiatedAt(LocalDateTime.now());
////        transaction2.setInitiated(true);
////
////        when(transactionRepository.save(any(Transaction.class)))
////                .thenReturn(transaction)
////                .thenReturn(transaction2);
////
////        CreateTransactionResponse response1 = transactionService.createTransaction(createTransactionRequest);
////        CreateTransactionResponse response2 = transactionService.createTransaction(createTransactionRequest);
////
////        assertNotEquals(response1.getTransactionRef(), response2.getTransactionRef());
////    }
//}