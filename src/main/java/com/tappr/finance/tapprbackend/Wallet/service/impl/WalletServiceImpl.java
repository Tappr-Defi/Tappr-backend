package com.tappr.finance.tapprbackend.Wallet.service.impl;

import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.tappr.finance.tapprbackend.Wallet.enums.CurrencyType;
import com.tappr.finance.tapprbackend.Wallet.service.interfaces.WalletService;
import com.tappr.finance.tapprbackend.user.data.models.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void createWalletForUser(User user) {
        // 1. Safety Check: Does user already have a wallet?
        // Note: Ensure your repository has boolean existsByUser(User user);
        if (walletRepository.existsByUser(user)) {
            return;
        }

        // 2. Generate Unique Account Number (NUBAN style - 10 digits)
        String accountNumber = generateUniqueAccountNumber();

        // 3. Create Entity
        Wallet wallet = new Wallet();
        wallet.setUser(user); // FIX: Set the User object, not just the ID string
        wallet.setAccountNumber(accountNumber);
        wallet.setFiatBalance(BigDecimal.ZERO);
        wallet.setSuiBalance(BigDecimal.ZERO);
        wallet.setCurrency("NGN");
        wallet.setType(CurrencyType.TIER_1); // Set a default type

        // 4. Save
        walletRepository.save(wallet);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            // Generate a random 10-digit number
            // Start with '2' or specific prefix to indicate it's internal/virtual if you like
            long number = 1_000_000_000L + secureRandom.nextLong(9_000_000_000L);
            accountNumber = String.valueOf(number).substring(0, 10);

        } while (walletRepository.existsByAccountNumber(accountNumber));
        // Loop until we find one that doesn't exist in the DB

        return accountNumber;
    }
}