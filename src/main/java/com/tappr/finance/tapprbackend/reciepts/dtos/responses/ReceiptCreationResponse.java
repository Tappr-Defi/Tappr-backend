package com.tappr.finance.tapprbackend.reciepts.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class ReceiptCreationResponse {

    private BigDecimal amountSent;      // e.g., 2000.00
    private String currency;            // e.g., "USD" or "NGN"
    private String status;              // "Transaction Successful"
    private String receiptNumber;

    // 2. Sender Details ("FROM")
    private String senderName;          // "Leonard Benjamin"
    private String senderIdentifier;    // "Tappr - 7890...879" (Masked Phone/Account)
    private String senderTimestamp;     // "March 20th, 2025 5:56PM"

    // 3. Recipient Details ("TO")
    private String recipientName;       // "Alicia Norbert" (or Merchant Name "Devon's Mart")
    private String recipientIdentifier; // "Tappr - 7090...079"
    private String recipientTimestamp;  // Usually same as sender timestamp

    // 4. Financial Specifics
    private BigDecimal transferCharge;  // "10.00"
    private String chargeCurrency;      // "NGN"
    private BigDecimal suiGasFee;       // "0.0000 SUI" (If applicable)

    // 5. Footer Details
    private String transferId;          // "#8567145245" (The unique Receipt No)
    private String tokensEarned;        // "50 Tappr" (Loyalty Points)

    // 6. Actionable Extras
    private String downloadUrl;         // Optional: Link to a PDF version if generated
}