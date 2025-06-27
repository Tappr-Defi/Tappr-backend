package com.semicolon.africa.tapprbackend.transaction.data.repositories;

import com.semicolon.africa.tapprbackend.transaction.data.models.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findBySymbol(String symbol);
}
