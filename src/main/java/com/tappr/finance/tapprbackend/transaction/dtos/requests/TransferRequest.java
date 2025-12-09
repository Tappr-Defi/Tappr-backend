package com.tappr.finance.tapprbackend.transaction.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Payload for initiating an internal P2P transfer")
public class TransferRequest {

    @NotBlank(message = "Recipient identifier is required")
    @Schema(description = "Email, Phone Number, or Username of the receiver", example = "friend@tappr.africa")
    private String recipientIdentifier;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Amount to transfer (Positive value)", example = "5000.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency code is required")
    @Schema(description = "Currency Code (NGN or SUI)", example = "NGN")
    private String currencyCode;

    @Schema(description = "Optional note for the transaction", example = "Lunch money 🍕")
    private String description;
}