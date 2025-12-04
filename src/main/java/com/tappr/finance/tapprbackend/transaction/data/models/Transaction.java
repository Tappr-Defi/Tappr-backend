package com.tappr.finance.tapprbackend.transaction.data.models;

import com.tappr.finance.tapprbackend.reciepts.data.models.Receipt;
import com.tappr.finance.tapprbackend.transaction.enums.CurrencyType;
import com.tappr.finance.tapprbackend.transaction.enums.TransactionStatus;
import com.tappr.finance.tapprbackend.user.data.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Getter
@Setter
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_ref", nullable = false, unique = true)
    private String transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private CurrencyType currency = CurrencyType.NGN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Receipt receipt;
}
