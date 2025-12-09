package com.tappr.finance.tapprbackend.ledger.data.repository;

import com.tappr.finance.tapprbackend.ledger.data.models.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
}
