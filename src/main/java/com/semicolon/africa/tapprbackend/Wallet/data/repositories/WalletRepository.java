package com.semicolon.africa.tapprbackend.Wallet.data.repositories;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
}
