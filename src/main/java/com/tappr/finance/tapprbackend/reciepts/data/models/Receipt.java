package com.tappr.finance.tapprbackend.reciepts.data.models;

import com.tappr.finance.tapprbackend.transaction.data.models.Transaction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "receipts")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false, unique = true)
    private String receiptNumber; // e.g., RCP-20250320-8567

    private String senderName;
    private String recipientName;
    private String senderAccountMasked; // e.g., **** 879
    private String recipientAccountMasked; // e.g., **** 079

    private String transactionFee; // "10.00 NGN"
    private String loyaltyPointsEarned; // "50 Tappr"

    @CreationTimestamp
    private LocalDateTime generatedAt;

    private String downloadUrl;
}