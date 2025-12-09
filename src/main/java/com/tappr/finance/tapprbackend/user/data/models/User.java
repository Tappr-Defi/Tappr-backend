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

    // ✅ FIX 1: Use snake_case, avoid spaces in DB columns
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String username;

    @Column(nullable = false)
    private String passwordHash;

    // ✅ FIX 2: Consistent naming
    @Column(nullable = false, unique = true, name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.REGULAR;

    // ✅ FIX 3: Make this private and initialize it
    // This tracks Email Verification status
    private boolean isVerified = false;

    private KycLevel kycLevel;

    private boolean isKycVerified = false;
    private boolean isTier2Verified = false;
    private boolean isLoggedIn = false;
    private boolean profileSetupComplete = false;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<KycDocument> kycDocuments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;

//    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Transaction> transactions;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private LoyaltyWallet loyaltyWallet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private MerchantProfile merchantProfile;

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}