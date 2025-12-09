package com.tappr.finance.tapprbackend.reciepts.data.repositories;

import com.tappr.finance.tapprbackend.reciepts.data.models.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    Optional<Receipt> findByTransactionId(UUID transactionId);
}
