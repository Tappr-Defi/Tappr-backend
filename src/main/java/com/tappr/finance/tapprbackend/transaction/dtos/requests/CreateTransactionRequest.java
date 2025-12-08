package com.tappr.finance.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class CreateTransactionRequest {

    private String merchantId;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private boolean status;
}

