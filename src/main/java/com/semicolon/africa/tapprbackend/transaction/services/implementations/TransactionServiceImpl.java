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
import com.semicolon.africa.tapprbackend.transaction.enums.CurrencyType;
import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
import com.semicolon.africa.tapprbackend.transaction.exceptions.InvalidRequestException;
import com.semicolon.africa.tapprbackend.transaction.exceptions.MerchantNotFoundException;
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
import java.util.Optional;
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
        String token = getTokenFromSpringContext(request);
        String email = jwtUtil.extractEmail(token);
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        String acNo = sender.getWallets().get(0).getAccountNumber();
        String suiAddress = sender.getWallets().get(0).getWalletAddress();

        if (!sender.isHasWallet()) {
            throw new InvalidRequestException("User does not have a wallet");
        }

        if (!sender.isLoggedIn()) {
            throw new InvalidRequestException("User is not logged in");
        }
        
        User receiver = userRepository.findUserByAccountNumber(request.getAccountNumber());
        if (receiver == null) {
            throw new InvalidRequestException("Receiver not found");
        }

        WalletCurrency currency = request.getAccountNumber().length() == 10 ? WalletCurrency.NGN : WalletCurrency.SUI;
        Wallet receiversWallet = walletRepository.findByUserAndCurrencyType(receiver, currency)
                .orElseThrow(() -> new InvalidRequestException("Receiver wallet not found"));

        String senderAccountNumber = sender.getWallets().stream()
                .filter(wallet -> wallet.getCurrencyType() == currency)
                .findFirst()
                .map(Wallet::getAccountNumber)
                .orElseThrow(() -> new InvalidRequestException("Sender wallet not found"));

        String senderWalletAddress = sender.getWallets().stream()
                .filter(wallet -> wallet.getCurrencyType() == currency)
                .findFirst()
                .map(Wallet::getWalletAddress)
                .orElseThrow(() -> new InvalidRequestException("Sender wallet address not found"));

//        String receiversFiatAccountNumber = receiversWallet.getAccountNumber();
//        String receiversSuiWalletAddress = receiversWallet.getWalletAddress();



        Transaction transaction = new Transaction();
        transaction.setTransactionRef(UUID.randomUUID().toString());
        transaction.setMerchant(receiver);
            Optional<Wallet> approvedWallet;
        if (currency.getWalletType().equals(WalletCurrency.NGN))
            approvedWallet = walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.NGN);
        else approvedWallet = walletRepository.findByUserAndCurrencyType(receiver, WalletCurrency.SUI);

        transaction.setSenderAccountNumber(acNo);
        transaction.setAmount(request.getAmount());
        transaction.setWalletCurrency(currency);
        transaction.setReceiversAccountNumber(request.getAccountNumber());
        transaction.setInitiatedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setInitiated(true);

        Receipt receipt = new Receipt();
        transaction.setReceipt(receipt);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToResponse(savedTransaction);

    }

    private static String getTokenFromSpringContext(CreateTransactionRequest request) {
        if (request.getAccountNumber() == null || request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount or account number must be correct and greater than zero!");
        }

        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidRequestException("Missing or invalid authorization header");
        }

        String token = authHeader.substring(7);
        return token;
    }


    private void validateRequest(CreateTransactionRequest request) {

    }


    private CreateTransactionResponse mapToResponse(Transaction transaction) {

    }
}