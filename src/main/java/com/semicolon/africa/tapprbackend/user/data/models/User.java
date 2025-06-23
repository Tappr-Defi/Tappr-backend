package com.semicolon.africa.tapprbackend.user.data.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.kyc.data.KycDocument;
import com.semicolon.africa.tapprbackend.notification.data.Notification;
import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import com.semicolon.africa.tapprbackend.Wallet.data.LoyaltyWallet;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "user_name", unique = true)
    private String userName;
    
    private String phone;
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.REGULAR; // Default role

    private boolean isKycVerified = false;
    private boolean isTier2Verified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Bidirectional relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KycDocument> kycDocuments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LoyaltyWallet loyaltyWallet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MerchantProfile merchantProfile;

    public User() {}

    public User(String email, String userName, Role role) {
        this.email = email;
        this.userName = userName;
        this.role = role;
    }
}
