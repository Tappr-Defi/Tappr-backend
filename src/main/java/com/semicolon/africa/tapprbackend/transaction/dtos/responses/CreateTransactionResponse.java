package com.semicolon.africa.tapprbackend.transaction.dtos.responses;

import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class CreateTransactionResponse {

    private Long transactionId;

    private String transactionRef;

    private String merchantName; // from User

    private BigDecimal amount;

    private String currency;

    private TransactionStatus status;

    private LocalDateTime initiatedAt;

    private LocalDateTime completedAt;

    private String receiptUrl; // or receipt ID if applicable
}
