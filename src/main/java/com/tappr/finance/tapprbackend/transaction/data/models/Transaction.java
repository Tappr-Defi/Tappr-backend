package com.tappr.finance.tapprbackend.transaction.data.models;

import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.transaction.enums.TransactionStatus;
import com.tappr.finance.tapprbackend.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_wallet_id")
    private Wallet destinationWallet;

    @Column(nullable = false, precision = 30, scale = 9)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String description;

    @CreationTimestamp
    private LocalDateTime initiatedAt;

    @UpdateTimestamp
    private LocalDateTime completedAt;
}