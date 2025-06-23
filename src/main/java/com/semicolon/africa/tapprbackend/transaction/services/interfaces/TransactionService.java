package com.semicolon.africa.tapprbackend.transaction.services.interfaces;

import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;

public interface TransactionService {
    CreateTransactionResponse createTransaction(CreateTransactionRequest request);
}
