package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import com.semicolon.africa.tapprbackend.transaction.data.repositories.TransactionRepository;
import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
import com.semicolon.africa.tapprbackend.transaction.enums.CurrencyType;
import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
import com.semicolon.africa.tapprbackend.transaction.services.interfaces.TransactionService;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public CreateTransactionResponse createTransaction(CreateTransactionRequest request) {
        // Find the user (merchant) by ID
        User merchant = userRepository.findById(UUID.fromString(request.getMerchantId()))
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        // Create and populate the transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionRef(UUID.randomUUID().toString());
        transaction.setMerchant(merchant);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(CurrencyType.valueOf(request.getCurrency()));
        transaction.setStatus(
                request.getStatus() != null
                        ? TransactionStatus.valueOf(request.getStatus().toUpperCase())
                        : TransactionStatus.PENDING
        );
        transaction.setInitiatedAt(LocalDateTime.now());

        // Save to DB
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Map and return response
        return mapToResponse(savedTransaction);
    }


    private CreateTransactionResponse mapToResponse(Transaction transaction) {
        CreateTransactionResponse response = new CreateTransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setTransactionRef(transaction.getTransactionRef());
        response.setMerchantName(transaction.getMerchant().getFullName()); // or getUserName()
        response.setAmount(transaction.getAmount());
        response.setCurrency(String.valueOf(transaction.getCurrency()).toUpperCase());
        response.setStatus(transaction.getStatus());
        response.setInitiatedAt(transaction.getInitiatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        if (transaction.getReceipt() != null) {
            response.setReceiptUrl(transaction.getReceipt().getDownloadUrl()); // assuming Receipt has this
        }

        return response;
    }
}
