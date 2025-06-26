package com.semicolon.africa.tapprbackend.Wallet.dtos.requests;

import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CreateWalletRequest {
    private WalletType type;
    private WalletCurrency currencyType;
}