package com.tappr.finance.tapprbackend.Wallet.data.repositories;

import com.tappr.finance.tapprbackend.Wallet.data.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    Optional<Currency> findById(String currencyCode);
}
