package com.tappr.finance.tapprbackend.Wallet.service.interfaces;

import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.user.data.models.User;
import jakarta.transaction.Transactional;

public interface WalletService {
    @Transactional
    Wallet createWallet(User user, String currencyCode);

    void createWalletForUser(User user);
}
