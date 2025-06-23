package com.semicolon.africa.tapprbackend.Wallet.data;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.time.LocalDateTime;

@Entity
public class LoyaltyWallet {
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private User user;

    private int points;
    private LocalDateTime lastUpdated;
}
