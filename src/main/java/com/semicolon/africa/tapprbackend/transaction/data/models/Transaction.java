package com.semicolon.africa.tapprbackend.transaction.data.models;

import com.semicolon.africa.tapprbackend.reciepts.data.models.Receipt;
import com.semicolon.africa.tapprbackend.transaction.enums.TransactionStatus;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;

    private String transactionRef;

    @ManyToOne
    private User merchant;

    private BigDecimal amount;
    private String currency; // NGN

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime initiatedAt;
    private LocalDateTime completedAt;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Receipt receipt;
}
