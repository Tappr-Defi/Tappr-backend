//package com.semicolon.africa.tapprbackend.transaction.services.implementations;
//
//import com.semicolon.africa.tapprbackend.reciepts.data.models.Receipt;
//import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
//import com.semicolon.africa.tapprbackend.transaction.data.repositories.TransactionRepository;
//import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
//import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
//import com.semicolon.africa.tapprbackend.transaction.enums.CurrencyType;
//import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
//import com.semicolon.africa.tapprbackend.transaction.exceptions.MerchantNotFoundException;
//import com.semicolon.africa.tapprbackend.transaction.services.interfaces.TransactionService;
//import com.semicolon.africa.tapprbackend.user.data.models.User;
//import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
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
//@SpringBootTest
//public class TransactionServiceImplTest {
//
//    @Autowired
//    private TransactionService transactionService;
//
//    @MockitoBean
//    private TransactionRepository transactionRepository;
//
//    @MockitoBean
//    private UserRepository userRepository;
//
//    private CreateTransactionRequest createTransactionRequest;
//    private User merchant;
//    private Transaction transaction;
//
//    @BeforeEach
//    public void setUp() {
//        // Reset mocks to ensure clean state
//        reset(transactionRepository, userRepository);
//
//        merchant = new User();
//        UUID merchantId = UUID.randomUUID();
//        merchant.setId(merchantId);
//        merchant.setFirstName("John");
//        merchant.setLastName("Doe");
//        merchant.setEmail("john.doe@example.com");
//        merchant.setPhoneNumber("+2348123456789");
//
//        createTransactionRequest = new CreateTransactionRequest();
//        createTransactionRequest.setMerchantId(String.valueOf(merchantId));
//        createTransactionRequest.setAmount(BigDecimal.valueOf(1000));
//        createTransactionRequest.setCurrency("NGN");
//
//        transaction = new Transaction();
//        transaction.setId(UUID.randomUUID().toString());
//        transaction.setTransactionRef(UUID.randomUUID().toString());
//        transaction.setMerchant(merchant);
//        transaction.setAmount(BigDecimal.valueOf(1000));
//        transaction.setCurrency(CurrencyType.NGN);
//        transaction.setStatus(TransactionStatus.PENDING);
//        transaction.setInitiatedAt(LocalDateTime.now());
//
//        // Setup mocks
//        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(merchant));
//        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
//            Transaction savedTransaction = invocation.getArgument(0);
//            // Simulate database behavior by setting a unique ID if not already set
//            if (savedTransaction.getId() == null) {
//                savedTransaction.setId(UUID.randomUUID().toString());
//            }
//            return savedTransaction;
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_successfullyCreatesTransaction() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//
//        assertNotNull(response);
//        assertNotNull(response.getTransactionId());
//        assertNotNull(response.getTransactionRef());
//        assertEquals(merchant.getFullName(), response.getMerchantName());
//        assertEquals(createTransactionRequest.getAmount(), response.getAmount());
//        assertEquals(createTransactionRequest.getCurrency(), response.getCurrency());
//        assertEquals(TransactionStatus.PENDING, response.getStatus());
//        assertNotNull(response.getInitiatedAt());
//
//        verify(userRepository, times(1)).findById(UUID.fromString(createTransactionRequest.getMerchantId()));
//        verify(transactionRepository, times(1)).save(any(Transaction.class));
//
//        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
//        verify(transactionRepository).save(transactionCaptor.capture());
//        Transaction savedTransaction = transactionCaptor.getValue();
//
//        assertEquals(merchant, savedTransaction.getMerchant());
//        assertEquals(createTransactionRequest.getAmount(), savedTransaction.getAmount());
//        assertEquals(CurrencyType.valueOf(createTransactionRequest.getCurrency()), savedTransaction.getCurrency());
//        assertEquals(TransactionStatus.PENDING, savedTransaction.getStatus());
//        assertNotNull(savedTransaction.getTransactionRef());
//        assertNotNull(savedTransaction.getInitiatedAt());
//    }
//
//    @Test
//    public void testCreateTransaction_withReceipt_returnsMerchantReceiptUrl() {
//        Receipt receipt = new Receipt();
//        receipt.setMerchantReceiptDownloadUrl("https://example.com/receipt.pdf");
//
//        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
//            Transaction savedTransaction = invocation.getArgument(0);
//            // Simulate database behavior by setting a unique ID if not already set
//            if (savedTransaction.getId() == null) {
//                savedTransaction.setId(UUID.randomUUID().toString());
//            }
//            // Add the receipt to the saved transaction
//            savedTransaction.setReceipt(receipt);
//            return savedTransaction;
//        });
//
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//
//        assertNotNull(response);
//        assertEquals(receipt.getMerchantReceiptDownloadUrl(), response.getMerchantReceiptDownloadUrl());
//    }
//
//    @Test
//    public void testCreateTransaction_withReceipt_returnsRecipientsReceiptUrl() {
//        Receipt receipt = new Receipt();
//        receipt.setRegularReceiptDownloadUrl("https://example.com/receipt.pdf");
//
//        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
//            Transaction savedTransaction = invocation.getArgument(0);
//            // Simulate database behavior by setting a unique ID if not already set
//            if (savedTransaction.getId() == null) {
//                savedTransaction.setId(UUID.randomUUID().toString());
//            }
//            // Add the receipt to the saved transaction
//            savedTransaction.setReceipt(receipt);
//            return savedTransaction;
//        });
//
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//
//        assertNotNull(response);
//        assertEquals(receipt.getRegularReceiptDownloadUrl(), response.getRegularReceiptDownloadUrl());
//    }
//
//    @Test
//    public void testCreateTransaction_merchantNotFound_throwsException() {
//        String merchantId = createTransactionRequest.getMerchantId();
//        when(userRepository.findById(UUID.fromString(merchantId))).thenReturn(Optional.empty());
//
//        MerchantNotFoundException exception = assertThrows(MerchantNotFoundException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//        assertEquals("Merchant not found", exception.getMessage());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldDefaultToPending() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertEquals(TransactionStatus.PENDING, response.getStatus());
//    }
//
//    @Test
//    public void testCreateTransaction_whenInvalidCurrency_shouldThrowException() {
//        createTransactionRequest.setCurrency("INVALID_CURRENCY");
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withValidDollarCurrency() {
//        createTransactionRequest.setCurrency("DOLLAR");
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//        assertEquals("DOLLAR", response.getCurrency());
//    }
//
//    @Test
//    public void testCreateTransaction_withValidEuroCurrency() {
//        System.out.println("Initial request currency: " + createTransactionRequest.getCurrency());
//        createTransactionRequest.setCurrency("EURO");
//        System.out.println("Updated request currency: " + createTransactionRequest.getCurrency());
//
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//
//        System.out.println("Response currency: " + response.getCurrency());
//        assertEquals("EURO", response.getCurrency());
//    }
//
//    @Test
//    public void testCreateTransaction_withValidPoundCurrency() {
//        createTransactionRequest.setCurrency("POUND");
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//        assertEquals("POUND", response.getCurrency());
//    }
//
//    @Test
//    public void testCreateTransaction_withLargeAmount() {
//        createTransactionRequest.setAmount(BigDecimal.valueOf(1000000));
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//        assertEquals(BigDecimal.valueOf(1000000), response.getAmount());
//    }
//
//    @Test
//    public void testCreateTransaction_withDecimalAmount() {
//        createTransactionRequest.setAmount(BigDecimal.valueOf(999.99));
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//        assertEquals(BigDecimal.valueOf(999.99), response.getAmount());
//    }
//
//    @Test
//    public void testCreateTransaction_withNegativeAmount_shouldThrowException() {
//        createTransactionRequest.setAmount(BigDecimal.valueOf(-100));
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withNullAmount_shouldThrowException() {
//        createTransactionRequest.setAmount(null);
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withNullMerchantId_shouldThrowException() {
//        createTransactionRequest.setMerchantId(null);
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withEmptyMerchantId_shouldThrowException() {
//        createTransactionRequest.setMerchantId("");
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withInvalidMerchantIdFormat_shouldThrowException() {
//        createTransactionRequest.setMerchantId("invalid-uuid-format");
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withNullCurrency_shouldThrowException() {
//        createTransactionRequest.setCurrency(null);
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withEmptyCurrency_shouldThrowException() {
//        createTransactionRequest.setCurrency("");
//        assertThrows(IllegalArgumentException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_shouldGenerateUniqueTransactionRefs() {
//        CreateTransactionResponse response1 = transactionService.createTransaction(createTransactionRequest);
//        CreateTransactionResponse response2 = transactionService.createTransaction(createTransactionRequest);
//
//        assertNotEquals(response1.getTransactionRef(), response2.getTransactionRef());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldGenerateUniqueTransactionIds() {
//        CreateTransactionResponse response1 = transactionService.createTransaction(createTransactionRequest);
//        CreateTransactionResponse response2 = transactionService.createTransaction(createTransactionRequest);
//
//        assertNotEquals(response1.getTransactionId(), response2.getTransactionId());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldSetCorrectMerchantName() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertEquals(merchant.getFullName(), response.getMerchantName());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldSetCorrectInitiatedTime() {
//        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
//
//        assertTrue(response.getInitiatedAt().isAfter(before));
//        assertTrue(response.getInitiatedAt().isBefore(after));
//    }
//
//    @Test
//    public void testCreateTransaction_shouldHaveNullCompletedAtInitially() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNull(response.getCompletedAt());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldSaveTransactionToRepository() {
//        transactionService.createTransaction(createTransactionRequest);
//        verify(transactionRepository, times(1)).save(any(Transaction.class));
//    }
//
//    @Test
//    public void testCreateTransaction_shouldCallUserRepositoryOnce() {
//        transactionService.createTransaction(createTransactionRequest);
//        verify(userRepository, times(1)).findById(UUID.fromString(createTransactionRequest.getMerchantId()));
//    }
//
//    @Test
//    public void testCreateTransaction_withDifferentMerchants() {
//        // Create another merchant
//        User anotherMerchant = new User();
//        UUID anotherMerchantId = UUID.randomUUID();
//        anotherMerchant.setId(anotherMerchantId);
//        anotherMerchant.setFirstName("Jane");
//        anotherMerchant.setLastName("Smith");
//        anotherMerchant.setEmail("jane.smith@example.com");
//
//        when(userRepository.findById(anotherMerchantId)).thenReturn(Optional.of(anotherMerchant));
//
//        createTransactionRequest.setMerchantId(String.valueOf(anotherMerchantId));
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//
//        assertEquals(anotherMerchant.getFullName(), response.getMerchantName());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldHandleMultipleConcurrentRequests() {
//        // Simulate multiple concurrent requests
//        CreateTransactionResponse response1 = transactionService.createTransaction(createTransactionRequest);
//        CreateTransactionResponse response2 = transactionService.createTransaction(createTransactionRequest);
//        CreateTransactionResponse response3 = transactionService.createTransaction(createTransactionRequest);
//
//        // All should be successful but unique
//        assertNotNull(response1);
//        assertNotNull(response2);
//        assertNotNull(response3);
//
//        assertNotEquals(response1.getTransactionId(), response2.getTransactionId());
//        assertNotEquals(response2.getTransactionId(), response3.getTransactionId());
//        assertNotEquals(response1.getTransactionId(), response3.getTransactionId());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldHandleRepositoryException() {
//        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("Database error"));
//
//        assertThrows(RuntimeException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_shouldHandleUserRepositoryException() {
//        when(userRepository.findById(any(UUID.class))).thenThrow(new RuntimeException("Database connection error"));
//
//        assertThrows(RuntimeException.class, () -> {
//            transactionService.createTransaction(createTransactionRequest);
//        });
//    }
//
//    @Test
//    public void testCreateTransaction_withVerySmallAmount() {
//        createTransactionRequest.setAmount(BigDecimal.valueOf(0.01));
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//        assertEquals(BigDecimal.valueOf(0.01), response.getAmount());
//    }
//
//    @Test
//    public void testCreateTransaction_withMaximumPrecisionAmount() {
//        BigDecimal preciseAmount = new BigDecimal("999.999999");
//        createTransactionRequest.setAmount(preciseAmount);
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//        assertEquals(preciseAmount, response.getAmount());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldPreserveCurrencyCase() {
//        createTransactionRequest.setCurrency("NGN");
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertEquals("NGN", response.getCurrency());
//    }
//
//    @Test
//    public void testCreateTransaction_shouldHandleWhitespaceInMerchantId() {
//        String merchantIdWithSpaces = "  " + createTransactionRequest.getMerchantId() + "  ";
//        createTransactionRequest.setMerchantId(merchantIdWithSpaces.trim());
//
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNotNull(response);
//    }
//
//    @Test
//    public void testCreateTransaction_shouldValidateTransactionRefFormat() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//
//        // Transaction ref should be a valid UUID format
//        assertDoesNotThrow(() -> UUID.fromString(response.getTransactionRef()));
//    }
//
//    @Test
//    public void testCreateTransaction_shouldValidateTransactionIdFormat() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//
//        // Transaction ID should be a valid UUID format or string
//        assertNotNull(response.getTransactionId());
//        assertFalse(response.getTransactionId().isEmpty());
//    }
//
//    @Test
//    public void testCreateTransaction_completedAtShouldBeNullByDefault() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertNull(response.getCompletedAt());
//    }
//
//    @Test
//    public void testCreateTransaction_transactionRefIsUuidFormat() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertDoesNotThrow(() -> UUID.fromString(response.getTransactionRef()));
//    }
//
//    @Test
//    public void testCreateTransaction_withZeroAmount_shouldAllowOrReject() {
//        createTransactionRequest.setAmount(BigDecimal.ZERO);
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        assertEquals(BigDecimal.ZERO, response.getAmount());
//    }
//
//    @Test
//    public void testCreateTransaction_initiatedAtShouldBeRecent() {
//        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
//
//        assertTrue(response.getInitiatedAt().isAfter(before));
//        assertTrue(response.getInitiatedAt().isBefore(after));
//    }
//
//    @Test
//    public void testCreateTransaction_shouldUseCorrectMerchantId() {
//        CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);
//        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
//        verify(transactionRepository).save(captor.capture());
//
//        Transaction savedTransaction = captor.getValue();
//        assertEquals(merchant.getId(), savedTransaction.getMerchant().getId());
//    }
//}