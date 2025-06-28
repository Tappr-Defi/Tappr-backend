package com.semicolon.africa.tapprbackend.reciepts.dtos.responses;

import com.semicolon.africa.tapprbackend.reciepts.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionReceiptResponse {
    private String transactionId;
    private TransactionType type;
    private String currency;
    private BigDecimal amount;
    private BigDecimal equivalentAmount; // e.g. 5000 NGN = 14.45 SUI
    private String equivalentCurrency;
    private BigDecimal rateUsed;
    private LocalDateTime timestamp;
}
