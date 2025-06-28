package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.reciepts.data.models.Receipt;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import com.semicolon.africa.tapprbackend.transaction.data.repositories.TransactionRepository;
import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
import com.semicolon.africa.tapprbackend.transaction.exceptions.InvalidRequestException;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Implementation Tests")
public class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private CreateTransactionRequest createTransactionRequest;
    private User sender;
    private User receiver;
    private Wallet senderFiatWallet;
    private Wallet senderCryptoWallet;
    private Wallet receiverWallet;
    private Transaction savedTransaction;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(UUID.randomUUID());
        sender.setFirstName("John");
        sender.setLastName("Doe");
        sender.setEmail("john.doe@example.com");
        sender.setPhoneNumber("+2348123456789");
        sender.setHasWallet(true);
        sender.setLoggedIn(true);

        receiver = new User();
        receiver.setId(UUID.randomUUID());
        receiver.setFirstName("Jane");
        receiver.setLastName("Smith");
        receiver.setEmail("jane.smith@example.com");
        receiver.setPhoneNumber("+2348987654321");
        receiver.setHasWallet(true);

        senderFiatWallet = new Wallet();
        senderFiatWallet.setId(UUID.randomUUID());
        senderFiatWallet.setUser(sender);
        senderFiatWallet.setCurrencyType(WalletCurrency.NGN);
        senderFiatWallet.setAccountNumber("1234567890");
        senderFiatWallet.setWalletAddress("0x123...fiat");
        senderFiatWallet.setBalance(BigDecimal.valueOf(10000));

        senderCryptoWallet = new Wallet();
        senderCryptoWallet.setId(UUID.randomUUID());
        senderCryptoWallet.setUser(sender);
        senderCryptoWallet.setCurrencyType(WalletCurrency.SUI);
        senderCryptoWallet.setAccountNumber("0987654321");
        senderCryptoWallet.setWalletAddress("0x456...crypto");
        senderCryptoWallet.setBalance(BigDecimal.valueOf(100));

        receiverWallet = new Wallet();
        receiverWallet.setId(UUID.randomUUID());
        receiverWallet.setUser(receiver);
        receiverWallet.setCurrencyType(WalletCurrency.NGN);
        receiverWallet.setAccountNumber("9876543210");
        receiverWallet.setWalletAddress("0x789...receiver");
        receiverWallet.setBalance(BigDecimal.valueOf(5000));

        createTransactionRequest = new CreateTransactionRequest();
        createTransactionRequest.setAccountNumber("9876543210");
        createTransactionRequest.setAmount(BigDecimal.valueOf(1000));

        savedTransaction = new Transaction();
        savedTransaction.setId(UUID.randomUUID().toString());
        savedTransaction.setTransactionRef(UUID.randomUUID().toString());
        savedTransaction.setMerchant(receiver);
        savedTransaction.setSenderAccountNumber(senderFiatWallet.getAccountNumber());
        savedTransaction.setReceiversAccountNumber(createTransactionRequest.getAccountNumber());
        savedTransaction.setAmount(createTransactionRequest.getAmount());
        savedTransaction.setWalletCurrency(WalletCurrency.NGN);
        savedTransaction.setStatus(TransactionStatus.PENDING);
        savedTransaction.setInitiatedAt(LocalDateTime.now());
        savedTransaction.setInitiated(true);
        savedTransaction.setReceipt(new Receipt());
    }

    @Nested
    @DisplayName("Successful Transaction Creation")
    class SuccessfulTransactionCreation {

        @Test
        @DisplayName("Should create transaction successfully with fiat account number")
        void shouldCreateTransactionSuccessfullyWithFiatAccountNumber() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupSuccessfulMocks(mockedRequestContextHolder);

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertNotNull(response);
                assertEquals(savedTransaction.getId(), response.getTransactionId());
                assertEquals(savedTransaction.getTransactionRef(), response.getTransactionRef());
                assertEquals(receiver.getFullName(), response.getMerchantName());
                assertEquals(createTransactionRequest.getAmount(), response.getAmount());
                assertEquals("NGN", response.getCurrency());
                assertEquals(TransactionStatus.PENDING, response.getStatus());
                assertNotNull(response.getInitiatedAt());
                assertNull(response.getCompletedAt());

                // Verify interactions
                verify(jwtUtil).extractEmail("valid-token");
                verify(userRepository).findByEmail(sender.getEmail());
                verify(userRepository).findUserByAccountNumber(createTransactionRequest.getAccountNumber());
                verify(walletRepository).findByUserAndCurrencyType(sender, WalletCurrency.NGN);
                verify(walletRepository).findByUserAndCurrencyType(sender, WalletCurrency.SUI);
                verify(walletRepository).findByUserAndCurrencyType(receiver, WalletCurrency.NGN);
                verify(transactionRepository).save(any(Transaction.class));
            }
        }

        @Test
        @DisplayName("Should create transaction successfully with crypto wallet address")
        void shouldCreateTransactionSuccessfullyWithCryptoWalletAddress() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                createTransactionRequest.setAccountNumber("0x789abcdef123456789abcdef123456789abcdef12");
                receiverWallet.setCurrencyType(WalletCurrency.SUI);
                savedTransaction.setWalletCurrency(WalletCurrency.SUI);
                
                setupCryptoTransactionMocks(mockedRequestContextHolder);
                when(userRepository.findUserByWalletAddress(createTransactionRequest.getAccountNumber()))
                        .thenReturn(Optional.of(receiver));
                when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.SUI))
                        .thenReturn(Optional.of(receiverWallet));

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertNotNull(response);
                assertEquals("SUI", response.getCurrency());
                verify(userRepository).findUserByWalletAddress(createTransactionRequest.getAccountNumber());
                verify(walletRepository).findByUserAndCurrencyType(receiver, WalletCurrency.SUI);
            }
        }

        private void setupCryptoTransactionMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                    .thenReturn(Optional.of(senderFiatWallet));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                    .thenReturn(Optional.of(senderCryptoWallet));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        }

        @Test
        @DisplayName("Should include receipt URLs when receipt exists")
        void shouldIncludeReceiptUrlsWhenReceiptExists() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                Receipt receipt = new Receipt();
                receipt.setMerchantReceiptDownloadUrl("https://example.com/merchant-receipt.pdf");
                receipt.setRegularReceiptDownloadUrl("https://example.com/regular-receipt.pdf");
                savedTransaction.setReceipt(receipt);
                
                setupSuccessfulMocks(mockedRequestContextHolder);

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertEquals(receipt.getMerchantReceiptDownloadUrl(), response.getMerchantReceiptDownloadUrl());
                assertEquals(receipt.getRegularReceiptDownloadUrl(), response.getRegularReceiptDownloadUrl());
            }
        }

        private void setupSuccessfulMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                    .thenReturn(Optional.of(receiver));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                    .thenReturn(Optional.of(senderFiatWallet));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                    .thenReturn(Optional.of(senderCryptoWallet));
            when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN))
                    .thenReturn(Optional.of(receiverWallet));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception when account number is null")
        void shouldThrowExceptionWhenAccountNumberIsNull() {
            createTransactionRequest.setAccountNumber(null);

            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                transactionService.createTransaction(createTransactionRequest);
            });
            assertEquals("Amount or account number must be correct and greater than zero!", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            createTransactionRequest.setAmount(null);

            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                transactionService.createTransaction(createTransactionRequest);
            });
            assertEquals("Amount or account number must be correct and greater than zero!", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is zero")
        void shouldThrowExceptionWhenAmountIsZero() {
            createTransactionRequest.setAmount(BigDecimal.ZERO);

            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                transactionService.createTransaction(createTransactionRequest);
            });
            assertEquals("Amount or account number must be correct and greater than zero!", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            createTransactionRequest.setAmount(BigDecimal.valueOf(-100));

            InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                transactionService.createTransaction(createTransactionRequest);
            });
            assertEquals("Amount or account number must be correct and greater than zero!", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should throw exception when authorization header is missing")
        void shouldThrowExceptionWhenAuthorizationHeaderIsMissing() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(servletRequestAttributes);
                when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
                when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Missing or invalid authorization header", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw exception when authorization header doesn't start with Bearer")
        void shouldThrowExceptionWhenAuthorizationHeaderDoesntStartWithBearer() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(servletRequestAttributes);
                when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
                when(httpServletRequest.getHeader("Authorization")).thenReturn("Invalid token");

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Missing or invalid authorization header", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw exception when user is not found")
        void shouldThrowExceptionWhenUserIsNotFound() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(servletRequestAttributes);
                when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
                when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
                when(jwtUtil.extractEmail("valid-token")).thenReturn("nonexistent@example.com");
                when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("User not found", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("User State Validation Tests")
    class UserStateValidationTests {

        @Test
        @DisplayName("Should throw exception when user doesn't have wallet")
        void shouldThrowExceptionWhenUserDoesntHaveWallet() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                sender.setHasWallet(false);
                setupAuthenticationMocks(mockedRequestContextHolder);

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("User does not have a wallet", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw exception when user is not logged in")
        void shouldThrowExceptionWhenUserIsNotLoggedIn() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                sender.setLoggedIn(false);
                setupAuthenticationMocks(mockedRequestContextHolder);

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("User is not logged in", exception.getMessage());
            }
        }

        private void setupAuthenticationMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        }
    }

    @Nested
    @DisplayName("Receiver Validation Tests")
    class ReceiverValidationTests {

        @Test
        @DisplayName("Should throw exception when receiver is not found by account number")
        void shouldThrowExceptionWhenReceiverIsNotFoundByAccountNumber() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupAuthenticationAndUserMocks(mockedRequestContextHolder);
                when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                        .thenReturn(Optional.empty());

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Receiver not found", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw exception when receiver is not found by wallet address")
        void shouldThrowExceptionWhenReceiverIsNotFoundByWalletAddress() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                createTransactionRequest.setAccountNumber("0x789abcdef123456789abcdef123456789abcdef12");
                setupAuthenticationAndUserMocks(mockedRequestContextHolder);
                when(userRepository.findUserByWalletAddress(createTransactionRequest.getAccountNumber()))
                        .thenReturn(Optional.empty());

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Receiver not found", exception.getMessage());
            }
        }

        private void setupAuthenticationAndUserMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        }
    }

    @Nested
    @DisplayName("Wallet Validation Tests")
    class WalletValidationTests {

        @Test
        @DisplayName("Should throw exception when sender fiat wallet is not found")
        void shouldThrowExceptionWhenSenderFiatWalletIsNotFound() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupAuthenticationUserAndReceiverMocks(mockedRequestContextHolder);
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                        .thenReturn(Optional.empty());

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Sender fiat wallet not found", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw exception when sender crypto wallet is not found")
        void shouldThrowExceptionWhenSenderCryptoWalletIsNotFound() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupAuthenticationUserAndReceiverMocks(mockedRequestContextHolder);
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                        .thenReturn(Optional.of(senderFiatWallet));
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                        .thenReturn(Optional.empty());

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Sender crypto wallet not found", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw exception when receiver wallet is not found")
        void shouldThrowExceptionWhenReceiverWalletIsNotFound() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupAuthenticationUserAndReceiverMocks(mockedRequestContextHolder);
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                        .thenReturn(Optional.of(senderFiatWallet));
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                        .thenReturn(Optional.of(senderCryptoWallet));
                when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN))
                        .thenReturn(Optional.empty());

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Receiver wallet not found", exception.getMessage());
            }
        }

        @Test
        @DisplayName("Should throw exception when sender has insufficient funds")
        void shouldThrowExceptionWhenSenderHasInsufficientFunds() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                senderFiatWallet.setBalance(BigDecimal.valueOf(500)); // Less than transaction amount
                createTransactionRequest.setAmount(BigDecimal.valueOf(1000));
                
                setupAuthenticationUserAndReceiverMocks(mockedRequestContextHolder);
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                        .thenReturn(Optional.of(senderFiatWallet));
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                        .thenReturn(Optional.of(senderCryptoWallet));
                when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN))
                        .thenReturn(Optional.of(receiverWallet));

                InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
                assertEquals("Insufficient funds in sender's fiat wallet", exception.getMessage());
            }
        }

        private void setupAuthenticationUserAndReceiverMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                    .thenReturn(Optional.of(receiver));
        }
    }

    @Nested
    @DisplayName("Currency Resolution Tests")
    class CurrencyResolutionTests {

        @Test
        @DisplayName("Should resolve NGN currency for Nigerian phone number with 10-digit account")
        void shouldResolveNGNCurrencyForNigerianPhoneNumberWith10DigitAccount() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                sender.setPhoneNumber("+2348123456789");
                createTransactionRequest.setAccountNumber("1234567890");
                
                setupSuccessfulTransactionMocks(mockedRequestContextHolder);
                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertEquals("NGN", response.getCurrency());
                verify(walletRepository).findByUserAndCurrencyType(receiver, WalletCurrency.NGN);
            }
        }

        @Test
        @DisplayName("Should resolve SUI currency for crypto wallet address")
        void shouldResolveSUICurrencyForCryptoWalletAddress() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                createTransactionRequest.setAccountNumber("0x789abcdef123456789abcdef123456789abcdef12");
                receiverWallet.setCurrencyType(WalletCurrency.SUI);
                savedTransaction.setWalletCurrency(WalletCurrency.SUI);
                
                setupCryptoTransactionMocks(mockedRequestContextHolder);
                when(userRepository.findUserByWalletAddress(createTransactionRequest.getAccountNumber()))
                        .thenReturn(Optional.of(receiver));
                when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.SUI))
                        .thenReturn(Optional.of(receiverWallet));

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertEquals("SUI", response.getCurrency());
                verify(walletRepository).findByUserAndCurrencyType(receiver, WalletCurrency.SUI);
            }
        }

        private void setupCryptoTransactionMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                    .thenReturn(Optional.of(senderFiatWallet));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                    .thenReturn(Optional.of(senderCryptoWallet));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        }

        private void setupSuccessfulTransactionMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                    .thenReturn(Optional.of(receiver));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                    .thenReturn(Optional.of(senderFiatWallet));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                    .thenReturn(Optional.of(senderCryptoWallet));
            when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN))
                    .thenReturn(Optional.of(receiverWallet));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        }
    }

    @Nested
    @DisplayName("Transaction Persistence Tests")
    class TransactionPersistenceTests {

        @Test
        @DisplayName("Should save transaction with correct properties")
        void shouldSaveTransactionWithCorrectProperties() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupSuccessfulMocks(mockedRequestContextHolder);

                transactionService.createTransaction(createTransactionRequest);

                verify(transactionRepository).save(argThat(transaction -> {
                    assertNotNull(transaction.getTransactionRef());
                    assertEquals(receiver, transaction.getMerchant());
                    assertEquals(senderFiatWallet.getAccountNumber(), transaction.getSenderAccountNumber());
                    assertEquals(createTransactionRequest.getAmount(), transaction.getAmount());
                    assertEquals(WalletCurrency.NGN, transaction.getWalletCurrency());
                    assertEquals(createTransactionRequest.getAccountNumber(), transaction.getReceiversAccountNumber());
                    assertNotNull(transaction.getInitiatedAt());
                    assertEquals(TransactionStatus.PENDING, transaction.getStatus());
                    assertTrue(transaction.isInitiated());
                    assertNotNull(transaction.getReceipt());
                    return true;
                }));
            }
        }

        @Test
        @DisplayName("Should generate unique transaction reference")
        void shouldGenerateUniqueTransactionReference() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupSuccessfulMocks(mockedRequestContextHolder);

                transactionService.createTransaction(createTransactionRequest);

                verify(transactionRepository).save(argThat(transaction -> {
                    assertNotNull(transaction.getTransactionRef());
                    assertFalse(transaction.getTransactionRef().isEmpty());
                    return true;
                }));
            }
        }

        private void setupSuccessfulMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                    .thenReturn(Optional.of(receiver));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                    .thenReturn(Optional.of(senderFiatWallet));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                    .thenReturn(Optional.of(senderCryptoWallet));
            when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN))
                    .thenReturn(Optional.of(receiverWallet));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle repository exceptions gracefully")
        void shouldHandleRepositoryExceptionsGracefully() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                setupAuthenticationAndUserMocks(mockedRequestContextHolder);
                when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                        .thenReturn(Optional.of(receiver));
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                        .thenReturn(Optional.of(senderFiatWallet));
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                        .thenReturn(Optional.of(senderCryptoWallet));
                when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN))
                        .thenReturn(Optional.of(receiverWallet));
                when(transactionRepository.save(any(Transaction.class)))
                        .thenThrow(new RuntimeException("Database connection error"));

                assertThrows(RuntimeException.class, () -> {
                    transactionService.createTransaction(createTransactionRequest);
                });
            }
        }

        @Test
        @DisplayName("Should handle large transaction amounts")
        void shouldHandleLargeTransactionAmounts() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                BigDecimal largeAmount = new BigDecimal("999999999.99");
                createTransactionRequest.setAmount(largeAmount);
                senderFiatWallet.setBalance(new BigDecimal("1000000000.00"));
                savedTransaction.setAmount(largeAmount);
                
                setupSuccessfulMocks(mockedRequestContextHolder);

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertEquals(largeAmount, response.getAmount());
            }
        }

        @Test
        @DisplayName("Should handle decimal amounts correctly")
        void shouldHandleDecimalAmountsCorrectly() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                BigDecimal decimalAmount = new BigDecimal("123.45");
                createTransactionRequest.setAmount(decimalAmount);
                savedTransaction.setAmount(decimalAmount);
                
                setupSuccessfulMocks(mockedRequestContextHolder);

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertEquals(decimalAmount, response.getAmount());
            }
        }

        private void setupAuthenticationAndUserMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
        }

        private void setupSuccessfulMocks(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                    .thenReturn(Optional.of(receiver));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                    .thenReturn(Optional.of(senderFiatWallet));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                    .thenReturn(Optional.of(senderCryptoWallet));
            when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN))
                    .thenReturn(Optional.of(receiverWallet));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        }
    }

    @Nested
    @DisplayName("Additional Edge Cases")
    class AdditionalEdgeCases {

        @Test
        @DisplayName("Should handle self-transaction (sender equals receiver)")
        void shouldHandleSelfTransaction() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                createTransactionRequest.setAccountNumber(senderFiatWallet.getAccountNumber()); // Same as sender
                
                mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                        .thenReturn(servletRequestAttributes);
                when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
                when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
                when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
                when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
                when(userRepository.findUserByAccountNumber(createTransactionRequest.getAccountNumber()))
                        .thenReturn(Optional.of(sender)); // Same user
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                        .thenReturn(Optional.of(senderFiatWallet));
                when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                        .thenReturn(Optional.of(senderCryptoWallet));
                
                savedTransaction.setMerchant(sender); // Self-transaction
                when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertNotNull(response);
                assertEquals(sender.getFullName(), response.getMerchantName());
            }
        }

        @Test
        @DisplayName("Should handle very long crypto wallet address")
        void shouldHandleVeryLongCryptoWalletAddress() {
            try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
                String longCryptoAddress = "0x" + "a".repeat(64); // Very long address
                createTransactionRequest.setAccountNumber(longCryptoAddress);
                receiverWallet.setCurrencyType(WalletCurrency.SUI);
                savedTransaction.setWalletCurrency(WalletCurrency.SUI);
                
                setupCryptoTransactionMocksForEdgeCases(mockedRequestContextHolder);
                when(userRepository.findUserByWalletAddress(longCryptoAddress))
                        .thenReturn(Optional.of(receiver));
                when(walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.SUI))
                        .thenReturn(Optional.of(receiverWallet));

                CreateTransactionResponse response = transactionService.createTransaction(createTransactionRequest);

                assertEquals("SUI", response.getCurrency());
            }
        }

        private void setupCryptoTransactionMocksForEdgeCases(MockedStatic<RequestContextHolder> mockedRequestContextHolder) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                    .thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer valid-token");
            when(jwtUtil.extractEmail("valid-token")).thenReturn(sender.getEmail());
            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN))
                    .thenReturn(Optional.of(senderFiatWallet));
            when(walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI))
                    .thenReturn(Optional.of(senderCryptoWallet));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        }
    }
}