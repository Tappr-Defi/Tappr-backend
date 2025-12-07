package com.tappr.finance.tapprbackend.Wallet.service.interfaces;

import com.tappr.finance.tapprbackend.user.data.models.User;

public interface WalletService {
    void createWalletForUser(User user);
}
