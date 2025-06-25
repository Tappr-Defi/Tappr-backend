package com.semicolon.africa.tapprbackend.user.data.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.kyc.data.models.KycDocument;
import com.semicolon.africa.tapprbackend.notification.data.Notification;
import com.semicolon.africa.tapprbackend.transaction.data.models.Transaction;
import com.semicolon.africa.tapprbackend.Wallet.data.LoyaltyWallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.REGULAR; // Default role

    private boolean isKycVerified = false;
    private boolean isTier2Verified = false;
    private boolean isLoggedIn = false;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = true)
    private Wallet wallet;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KycDocument> kycDocuments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LoyaltyWallet loyaltyWallet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private MerchantProfile merchantProfile;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
