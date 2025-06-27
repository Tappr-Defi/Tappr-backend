package com.semicolon.africa.tapprbackend.Wallet.data.model;

import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletStatus;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_number", unique = true)
    private String accountNumber; // for fiat wallets only

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false)
    private WalletType walletType; // FIAT or CRYPTO

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false)
    private WalletCurrency currencyType; // NGN, SUI, USDT, etc.

    @Column(name = "wallet_address", unique = true)
    private String walletAddress; // for crypto wallets only

    @Column(name = "token_symbol")
    private String tokenSymbol; // e.g., "SUI" for crypto, optional

    @Enumerated(EnumType.STRING)
    private WalletStatus status = WalletStatus.ACTIVE;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
