package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.WalletBalanceResponse;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.service.interfaces.WalletService;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.services.interfaces.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final JwtUtil jwtUtil;
    private final WalletService walletService;
    private final WalletRepository walletRepository;

    public UserServiceImpl(JwtUtil jwtUtil, WalletService walletService, WalletRepository walletRepository) {
        this.jwtUtil = jwtUtil;
        this.walletService = walletService;
        this.walletRepository = walletRepository;
    }

    @Override
    public LoginResponse generateLoginResponse(User user, String message) {
        WalletBalanceResponse walletBalances = null;
        String suiWalletAddress = null;
        String fiatWalletAccountNumber = null;
        boolean hasSuiWallet = false;
        boolean hasFiatWallet = false;

        try {
            walletBalances = walletService.getUserWalletBalances(user.getId());

            Wallet sui = walletRepository.findByUserAndCurrencyType(user, WalletCurrency.SUI).orElse(null);
            if (sui != null) {
                hasSuiWallet = true;
                suiWalletAddress = sui.getWalletAddress();
            }

            Wallet fiat = walletRepository.findByUserAndCurrencyType(user, WalletCurrency.NGN).orElse(null);
            if (fiat != null) {
                hasFiatWallet = true;
                fiatWalletAccountNumber = fiat.getAccountNumber();
            }
        } catch (Exception e) {
            log.warn("Could not retrieve wallet information for user {}: {}", user.getEmail(), e.getMessage());
        }

        return new LoginResponse(
                message,
                jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole()),
                jwtUtil.generateRefreshToken(user.getEmail(), user.getId(), user.getRole()),
                user.getRole(),
                true,
                String.valueOf(user.getId()),
                hasSuiWallet,
                hasFiatWallet,
                fiatWalletAccountNumber,
                suiWalletAddress,
                walletBalances
        );
    }


}
