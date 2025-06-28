package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.enums.CurrencyResolver;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.reciepts.data.models.Receipt;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import com.semicolon.africa.tapprbackend.transaction.data.repositories.TransactionRepository;
import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
import com.semicolon.africa.tapprbackend.transaction.exceptions.InvalidRequestException;
import com.semicolon.africa.tapprbackend.transaction.services.interfaces.TransactionService;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WalletRepository walletRepository;

    @Override
    public CreateTransactionResponse createTransaction(CreateTransactionRequest request) {
        if (request.getAccountNumber() == null || request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount or account number must be correct and greater than zero!");
        }

        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidRequestException("Missing or invalid authorization header");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (!sender.isHasWallet()) {
            throw new InvalidRequestException("User does not have a wallet");
        }

        if (!sender.isLoggedIn()) {
            throw new InvalidRequestException("User is not logged in");
        }

        // Determine target currency based on input format
        WalletCurrency targetCurrency = resolveCurrencyFromInput(sender.getPhoneNumber(), request.getAccountNumber());
        
        // Find receiver by account number or wallet address
        User receiver = findReceiverByIdentifier(request.getAccountNumber(), targetCurrency);
        if (receiver == null) {
            throw new InvalidRequestException("Receiver not found");
        }

        // Get sender's fiat wallet (users always spend from fiat wallet - Naira)
        Wallet senderFiatWallet = walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN)
                .orElseThrow(() -> new InvalidRequestException("Sender fiat wallet not found"));

        // Get sender's crypto wallet (for Sui execution)
        Wallet senderCryptoWallet = walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI)
                .orElseThrow(() -> new InvalidRequestException("Sender crypto wallet not found"));

        // Get receiver's target wallet
        Wallet receiversWallet = walletRepository.findByUserAndCurrencyType(receiver, targetCurrency)
                .orElseThrow(() -> new InvalidRequestException("Receiver wallet not found"));

        // Validate sender has sufficient balance in fiat wallet (since users spend Naira)
        if (senderFiatWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InvalidRequestException("Insufficient funds in sender's fiat wallet");
        }

        // For now, we're just creating the transaction record
        // The actual balance updates and Sui execution would happen in a separate service
        // TODO: Implement actual balance transfer and Sui blockchain execution

        Transaction transaction = new Transaction();
        transaction.setTransactionRef(UUID.randomUUID().toString());
        transaction.setMerchant(receiver);

        // Store sender's fiat account number (what user sees)
        transaction.setSenderAccountNumber(senderFiatWallet.getAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setWalletCurrency(targetCurrency);

        // Store receiver's identifier (account number or wallet address)
        transaction.setReceiversAccountNumber(request.getAccountNumber());
        transaction.setInitiatedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setInitiated(true);

        Receipt receipt = new Receipt();
        transaction.setReceipt(receipt);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToResponse(savedTransaction);
    }

    private WalletCurrency resolveCurrencyFromInput(String senderPhone, String input) {
        if (input.length() == 10 && input.matches("\\d+")) {
            com.semicolon.africa.tapprbackend.transaction.enums.WalletCurrency transactionCurrency =
                CurrencyResolver.resolveCurrencyFromCountry(senderPhone);
            return convertToWalletCurrency(transactionCurrency);
        }
        return WalletCurrency.SUI;
    }

    /**
     * Converts transaction package WalletCurrency to Wallet package WalletCurrency
     */
    private WalletCurrency convertToWalletCurrency(com.semicolon.africa.tapprbackend.transaction.enums.WalletCurrency transactionCurrency) {
        return switch (transactionCurrency) {
            // Direct mappings for currencies that exist in both enums
            case NGN -> WalletCurrency.NGN;
            case KES -> WalletCurrency.KES;
            case GHS -> WalletCurrency.GHS;
            case ZAR -> WalletCurrency.ZAR;
            case EGP -> WalletCurrency.EGP;
            case DZD -> WalletCurrency.DZD;
            case MAD -> WalletCurrency.MAD;
            case UGX -> WalletCurrency.UGX;
            case TZS -> WalletCurrency.TZS;
            case ETB -> WalletCurrency.ETB;
            case XAF -> WalletCurrency.XAF;
            case XOF -> WalletCurrency.XOF;
            case RWF -> WalletCurrency.RWF;
            case BWP -> WalletCurrency.BWP;
            case MZN -> WalletCurrency.MZN;
            case USD -> WalletCurrency.USD;
            case GBP -> WalletCurrency.GBP;
            case EUR -> WalletCurrency.EUR;
            case INR -> WalletCurrency.INR;
            case JPY -> WalletCurrency.JPY;
            case AED -> WalletCurrency.AED;
            case SUI -> WalletCurrency.SUI;

            // Mappings for currencies that have different names or don't exist in Wallet enum
            case DOLLAR -> WalletCurrency.USD;
            case EURO -> WalletCurrency.EUR;
            case POUND -> WalletCurrency.GBP;
            case YEN -> WalletCurrency.JPY;

            // For currencies that don't exist in Wallet enum, map to closest equivalent or fallback
            case TND, AOA, CDF, MWK, ZMW, ZWL, LSL, NAD, GMD, SLL, LRD, GNF, MRU,
                 SCR, CVE, DJF, ERN, KMF, STN, SZL, SOS, SSP, CNY, AUD, CAD, BRL -> WalletCurrency.USD;

            default -> WalletCurrency.SUI; // fallback
        };
    }

    private CreateTransactionResponse mapToResponse(Transaction transaction) {
        CreateTransactionResponse response = new CreateTransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setTransactionRef(transaction.getTransactionRef());
        response.setMerchantName(transaction.getMerchant().getFullName());
        response.setAmount(transaction.getAmount());
        response.setCurrency(String.valueOf(transaction.getWalletCurrency()).toUpperCase());
        response.setStatus(transaction.getStatus());
        response.setInitiatedAt(transaction.getInitiatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        if (transaction.getReceipt() != null) {
            response.setMerchantReceiptDownloadUrl(transaction.getReceipt().getMerchantReceiptDownloadUrl());
            response.setRegularReceiptDownloadUrl(transaction.getReceipt().getRegularReceiptDownloadUrl());
        }

        return response;
    }

    /**
     * Finds receiver by account number (for fiat) or wallet address (for crypto)
     */
    private User findReceiverByIdentifier(String identifier, WalletCurrency currency) {
        if (identifier.length() == 10 && identifier.matches("\\d+")) {
            // Fiat account number - search by account number
            return userRepository.findUserByAccountNumber(identifier)
                    .orElse(null);
        } else {
            // Crypto wallet address - search by wallet address
            return userRepository.findUserByWalletAddress(identifier)
                    .orElse(null);
        }
    }

    /**
     * Helper method to get all necessary wallet information for a transaction
     * This demonstrates how to retrieve both fiat and crypto wallet details
     */
    private TransactionWalletInfo getTransactionWalletInfo(User sender, User receiver, String receiverIdentifier) {
        // Get sender's fiat wallet (what user spends from)
        Wallet senderFiatWallet = walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.NGN)
                .orElseThrow(() -> new InvalidRequestException("Sender fiat wallet not found"));

        // Get sender's crypto wallet (for Sui execution)
        Wallet senderCryptoWallet = walletRepository.findByUserAndCurrencyType(sender, WalletCurrency.SUI)
                .orElseThrow(() -> new InvalidRequestException("Sender crypto wallet not found"));

        // Determine receiver's wallet type based on identifier
        WalletCurrency receiverCurrency = receiverIdentifier.length() == 10 ? WalletCurrency.NGN : WalletCurrency.SUI;

        // Get receiver's target wallet
        Wallet receiverWallet = walletRepository.findByUserAndCurrencyType(receiver, receiverCurrency)
                .orElseThrow(() -> new InvalidRequestException("Receiver wallet not found"));

        return new TransactionWalletInfo(
            senderFiatWallet.getAccountNumber(),
            senderFiatWallet.getWalletAddress(),
            senderCryptoWallet.getAccountNumber(),
            senderCryptoWallet.getWalletAddress(),
            receiverWallet.getAccountNumber(),
            receiverWallet.getWalletAddress(),
            receiverCurrency
        );
    }
    
    /**
     * Inner class to hold wallet information for transactions
     */
    private static class TransactionWalletInfo {
        public final String senderFiatAccountNumber;
        public final String senderFiatWalletAddress;
        public final String senderCryptoAccountNumber;
        public final String senderCryptoWalletAddress;
        public final String receiverAccountNumber;
        public final String receiverWalletAddress;
        public final WalletCurrency receiverCurrency;
        
        public TransactionWalletInfo(String senderFiatAccountNumber, String senderFiatWalletAddress,
                                   String senderCryptoAccountNumber, String senderCryptoWalletAddress,
                                   String receiverAccountNumber, String receiverWalletAddress,
                                   WalletCurrency receiverCurrency) {
            this.senderFiatAccountNumber = senderFiatAccountNumber;
            this.senderFiatWalletAddress = senderFiatWalletAddress;
            this.senderCryptoAccountNumber = senderCryptoAccountNumber;
            this.senderCryptoWalletAddress = senderCryptoWalletAddress;
            this.receiverAccountNumber = receiverAccountNumber;
            this.receiverWalletAddress = receiverWalletAddress;
            this.receiverCurrency = receiverCurrency;
        }
    }
}
