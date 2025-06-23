package com.semicolon.africa.tapprbackend.reciepts.data.models;

import jakarta.persistence.*;
import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;

import java.time.LocalDateTime;

@Entity
public class Receipt {
    @Id @GeneratedValue
    private Long id;

    @OneToOne
    private Transaction transaction;

    private String blockchainHash;
    private String receiptUrl; // IPFS/Walrus link or CDN

    private LocalDateTime issuedAt;
}
