package com.tappr.finance.tapprbackend.user.data.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.kyc.enums.KycLevel;
import com.tappr.finance.tapprbackend.user.enums.Role;
import com.tappr.finance.tapprbackend.kyc.data.models.KycDocument;
import com.tappr.finance.tapprbackend.notification.data.Notification;
import com.tappr.finance.tapprbackend.transaction.data.models.Transaction;
import com.tappr.finance.tapprbackend.Wallet.data.model.LoyaltyWallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.REGULAR;

    private KycLevel kycLevel;

    private boolean isKycVerified = false;
    private boolean isTier2Verified = false;
    private boolean isLoggedIn = false;
    private boolean profileSetupComplete;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
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

    public boolean isVerified;
}
