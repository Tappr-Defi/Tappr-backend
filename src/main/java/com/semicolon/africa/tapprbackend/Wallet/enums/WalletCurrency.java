
package com.semicolon.africa.tapprbackend.Wallet.enums;

import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import lombok.Getter;

@Getter
public enum WalletCurrency {
    // --- FIAT CURRENCIES ---
    NGN(WalletType.FIAT), // Nigeria
    KES(WalletType.FIAT), // Kenya
    GHS(WalletType.FIAT), // Ghana
    ZAR(WalletType.FIAT), // South Africa
    XOF(WalletType.FIAT), // West African CFA franc (e.g., Senegal, Côte d’Ivoire)
    XAF(WalletType.FIAT), // Central African CFA franc (e.g., Cameroon, Chad)
    UGX(WalletType.FIAT), // Uganda
    TZS(WalletType.FIAT), // Tanzania
    RWF(WalletType.FIAT), // Rwanda
    BWP(WalletType.FIAT), // Botswana
    MAD(WalletType.FIAT), // Morocco
    DZD(WalletType.FIAT), // Algeria
    EGP(WalletType.FIAT), // Egypt
    SDG(WalletType.FIAT), // Sudan
    ETB(WalletType.FIAT), // Ethiopia
    MZN(WalletType.FIAT), // Mozambique

    USD(WalletType.FIAT), // US Dollar
    GBP(WalletType.FIAT), // British Pound
    EUR(WalletType.FIAT), // Euro
    INR(WalletType.FIAT), // Indian Rupee
    JPY(WalletType.FIAT), // Japanese Yen
    AED(WalletType.FIAT), // UAE Dirham

    // --- CRYPTO CURRENCIES ---
    BTC(WalletType.CRYPTO),
    ETH(WalletType.CRYPTO),
    USDT(WalletType.CRYPTO),
    SUI(WalletType.CRYPTO);

    private final WalletType walletType;

    WalletCurrency(WalletType walletType) {
        this.walletType = walletType;
    }
}
