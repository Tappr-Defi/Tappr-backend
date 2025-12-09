package com.tappr.finance.tapprbackend.transaction.dtos.requests;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Builder
public class DepositFundRequest {
    private UUID userId;
    private BigDecimal amount;
    private String currencyCode;
    private String paymentMethod;
}
