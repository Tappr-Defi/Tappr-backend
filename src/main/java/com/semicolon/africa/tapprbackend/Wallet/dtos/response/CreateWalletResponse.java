
package com.semicolon.africa.tapprbackend.Wallet.dtos.response;

import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletStatus;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
public class CreateWalletResponse {
    private String message;
    private WalletStatus walletStatus;
    private String accountNumber;
    private WalletType walletType;
    private WalletCurrency walletCurrency;
    private BigDecimal balance;


}
