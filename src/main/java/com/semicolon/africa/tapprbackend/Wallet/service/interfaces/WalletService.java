package com.semicolon.africa.tapprbackend.Wallet.service.interfaces;

import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.CreateWalletResponse;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import org.springframework.stereotype.Component;

@Component
public interface WalletService {

    CreateWalletResponse createWalletForUser(String jwtToken, CreateWalletRequest createWalletRequest);

    void createWalletIfNotExists(User user);
    CreateWalletResponse createWallet(User user);
}
