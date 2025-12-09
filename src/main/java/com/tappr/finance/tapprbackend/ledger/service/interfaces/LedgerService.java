package com.tappr.finance.tapprbackend.ledger.service.interfaces;

import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.transaction.data.models.Transaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface LedgerService {
    @Transactional
    void debitWallet(Wallet wallet, BigDecimal amount, Transaction transaction, String description);

    @Transactional
    void creditWallet(Wallet wallet, BigDecimal amount, Transaction transaction, String description);
}
