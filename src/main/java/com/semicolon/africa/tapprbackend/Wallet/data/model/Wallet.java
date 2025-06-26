

package com.semicolon.africa.tapprbackend.Wallet.data.model;

import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletStatus;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@ToString
@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id", columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false)
    private WalletType walletType;

    @Column(name = "currency_type", nullable = false)
    private WalletCurrency currencyType;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private WalletStatus status;

}
