package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateTransactionRequest {

    private String merchantId;

    private BigDecimal amount;

    private String currency = "NGN";

    // Optional: could be set to PENDING by default in the service layer
    private String status;
}

