package com.tappr.finance.tapprbackend.Wallet.data.model;

import com.tappr.finance.tapprbackend.Wallet.enums.CurrencyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "currencies")
public class Currency {
    @Id
    @Column(length = 10)
    private String code;

    private String name;

    @Enumerated(EnumType.STRING)
    private CurrencyType type;

    private int decimalPlaces; // 2 for NGN, 9 for SUI

    private boolean isActive;
}