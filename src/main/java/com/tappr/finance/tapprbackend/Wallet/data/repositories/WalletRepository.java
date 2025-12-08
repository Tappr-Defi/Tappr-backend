package com.tappr.finance.tapprbackend.Wallet.data.repositories;

import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.user.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    boolean existsByUser(User user);
    Optional<Wallet> findByUserAndCurrencyCode(User user, String currencyCode);

    @Query(value = "SELECT nextval('wallet_derivation_seq')", nativeQuery = true)
    Long getNextDerivationIndex();

    boolean existsByAccountNumber(String accountNumber);
}
