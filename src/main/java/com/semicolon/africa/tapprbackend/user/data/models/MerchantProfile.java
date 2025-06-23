package com.semicolon.africa.tapprbackend.user.data.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class MerchantProfile {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private User user;

    private String businessName;
    private String businessType;
    private String bankAccountNumber;
    private String bankName;

    private String deviceId; // For SoftPOS linking
}
