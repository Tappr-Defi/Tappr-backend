package com.semicolon.africa.tapprbackend.Wallet.service.interfaces;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import org.springframework.stereotype.Component;

@Component
public interface WalletService {

    void createWalletIfNotExists(User user);
}
