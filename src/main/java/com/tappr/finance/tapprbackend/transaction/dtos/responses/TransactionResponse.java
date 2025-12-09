package com.tappr.finance.tapprbackend.transaction.dtos.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Receipt details for a successful transaction")
public class TransactionResponse {

    @Schema(description = "Unique Reference for tracking", example = "TXN-A1B2C3D4")
    private String transactionRef;

    @Schema(description = "Amount processed", example = "5000.00")
    private BigDecimal amount;

    @Schema(description = "Current status of the transfer", example = "SUCCESS")
    private String status;

    @Schema(description = "Human-readable result message", example = "Transfer successful")
    private String message;
}