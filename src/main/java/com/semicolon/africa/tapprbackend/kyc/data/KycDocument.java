package com.semicolon.africa.tapprbackend.kyc.data;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class KycDocument {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    private String type; // e.g., BVN, NIN
    private String documentUrl;
    private boolean verified;

    private LocalDateTime uploadedAt;
}
