package com.tappr.finance.tapprbackend.transaction.services.interfaces;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.requests.DepositFundRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.requests.TransferRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
import com.tappr.finance.tapprbackend.transaction.dtos.responses.TransactionResponse;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {
    @Transactional
    ApiResponse<TransactionResponse> transferFunds(TransferRequest request);
    ApiResponse<TransactionResponse> depositFunds(DepositFundRequest request);
}
