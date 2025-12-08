package com.tappr.finance.tapprbackend.Wallet.data.model;

import com.tappr.finance.tapprbackend.user.data.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "currency_code"}),
        @UniqueConstraint(columnNames = {"deposit_address"}),
        @UniqueConstraint(columnNames = {"derivation_index"})
})
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_code", nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    private String accountNumber;

    private String depositAddress;

    private Long derivationIndex;

    @Column(nullable = false)
    private boolean isOmnibus = false;
}