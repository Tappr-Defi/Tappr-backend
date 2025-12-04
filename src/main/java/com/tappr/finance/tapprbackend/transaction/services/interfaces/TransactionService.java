package com.tappr.finance.tapprbackend.transaction.services.interfaces;

import com.tappr.finance.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;

public interface TransactionService {
    CreateTransactionResponse createTransaction(CreateTransactionRequest request);
}
