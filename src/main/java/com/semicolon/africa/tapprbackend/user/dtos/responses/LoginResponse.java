package com.semicolon.africa.tapprbackend.user.dtos.responses;

import com.semicolon.africa.tapprbackend.Wallet.dtos.response.WalletBalanceResponse;
import lombok.*;
import com.semicolon.africa.tapprbackend.user.enums.Role;

@Data
@ToString
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private Role role;
    private boolean loggedIn;
    private String userId;
    private boolean hasSuiWallet;
    private boolean hasFiatWallet;
    private String fiatWalletAccountNumber;
    private String suiWalletAddress;
    private WalletBalanceResponse walletBalances;
}


