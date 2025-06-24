package com.semicolon.africa.tapprbackend.user.data.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "merchant_profiles")
public class MerchantProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;
    
    @Column(name = "business_type", nullable = false)
    private String businessType;
    
    @Column(name = "bank_account_number", nullable = false)
    private String bankAccountNumber;
    
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "device_id")
    private String deviceId; // For SoftPOS linking
}
