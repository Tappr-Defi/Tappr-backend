package com.semicolon.africa.tapprbackend.reciepts.data.models;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.*;
import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "receipts")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = true)
    private Transaction transaction;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "sender_id", referencedColumnName = "id", nullable = true)
    private User senderDetails;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", nullable = true)
    private User recipientDetails;

    @Column(name = "blockchain_hash")
    private String blockchainHash;

    @Column(name = "merchant_receipt_url", nullable = false)
    private String merchantReceiptDownloadUrl; // IPFS/Walrus link or CDN

    @Column(name = "regular_receipt_url", nullable = false)
    private String regularReceiptDownloadUrl; // IPFS/Walrus link or CDN

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    private boolean isMerchant;

}
