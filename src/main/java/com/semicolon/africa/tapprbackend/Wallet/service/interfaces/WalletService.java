package com.semicolon.africa.tapprbackend.Wallet.service.interfaces;

import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.CreateWalletResponse;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.WalletBalanceResponse;
import com.semicolon.africa.tapprbackend.user.data.models.User;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    CreateWalletResponse createWalletForUser(String jwtToken, CreateWalletRequest createWalletRequest);

    void createWalletIfNotExists(User user);

    CreateWalletResponse createWallet(User user);

    void depositFiat(UUID userId, BigDecimal amount);

    void withdrawFiat(UUID userId, BigDecimal amount);

    void depositSui(UUID userId, BigDecimal amount);

    void withdrawSui(UUID userId, BigDecimal amount);

    WalletBalanceResponse getUserWalletBalances(UUID userId);
}
