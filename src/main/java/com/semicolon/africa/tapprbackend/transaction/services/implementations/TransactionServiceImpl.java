package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import com.semicolon.africa.tapprbackend.transaction.data.repositories.TransactionRepository;
import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
import com.semicolon.africa.tapprbackend.transaction.enums.CurrencyType;
import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
import com.semicolon.africa.tapprbackend.transaction.exceptions.MerchantNotFoundException;
import com.semicolon.africa.tapprbackend.transaction.services.interfaces.TransactionService;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public CreateTransactionResponse createTransaction(CreateTransactionRequest request) {

        User merchant = userRepository.findById(UUID.fromString(request.getMerchantId()))
                .orElseThrow(() -> new MerchantNotFoundException("Merchant not found"));


        Transaction transaction = new Transaction();
        transaction.setTransactionRef(UUID.randomUUID().toString());
        transaction.setMerchant(merchant);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(CurrencyType.valueOf(request.getCurrency()));
        transaction.setInitiatedAt(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setInitiated(true);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToResponse(savedTransaction);
    }


    private CreateTransactionResponse mapToResponse(Transaction transaction) {
        CreateTransactionResponse response = new CreateTransactionResponse();
        response.setTransactionId(transaction.getId());
        response.setTransactionRef(transaction.getTransactionRef());
        response.setMerchantName(transaction.getMerchant().getFullName());
        response.setAmount(transaction.getAmount());
        response.setCurrency(String.valueOf(transaction.getCurrency()).toUpperCase());
        response.setStatus(transaction.getStatus());
        response.setInitiatedAt(transaction.getInitiatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        if (transaction.getReceipt() != null) {
            response.setMerchantReceiptDownloadUrl(transaction.getReceipt().getMerchantReceiptDownloadUrl());
            response.setRegularReceiptDownloadUrl(transaction.getReceipt().getRegularReceiptDownloadUrl());
        }

        return response;
    }
}