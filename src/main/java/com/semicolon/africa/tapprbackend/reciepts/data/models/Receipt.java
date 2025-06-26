package com.semicolon.africa.tapprbackend.reciepts.data.models;

import jakarta.persistence.*;
import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "receipts")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "blockchain_hash")
    private String blockchainHash;
    
    @Column(name = "receipt_url", nullable = false)
    private String receiptUrl; // IPFS/Walrus link or CDN

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    public String getDownloadUrl() {
        return "";
    }

    public void setDownloadUrl(String url) {

    }
}
