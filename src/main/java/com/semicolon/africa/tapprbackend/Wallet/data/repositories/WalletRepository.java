package com.semicolon.africa.tapprbackend.Wallet.data.repositories;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    boolean existsByUser(User user);
    Optional<Wallet> findByUserAndCurrencyType(User user, WalletCurrency currencyType);

    boolean existsByUserAndCurrencyType(User user, WalletCurrency walletCurrency);
}
