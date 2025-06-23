package com.semicolon.africa.tapprbackend.Wallet.data.model;

import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String accountNumber;
    private BigDecimal balance;
    private WalletType type;

}
