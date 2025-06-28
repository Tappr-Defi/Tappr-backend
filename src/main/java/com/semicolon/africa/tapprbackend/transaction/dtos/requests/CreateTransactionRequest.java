package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CreateTransactionRequest {

//    private String merchantId;
    private String accountNumber;
    private BigDecimal amount;

//    private String currency;

}

