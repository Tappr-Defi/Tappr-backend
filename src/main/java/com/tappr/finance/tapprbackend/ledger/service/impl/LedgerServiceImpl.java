package com.tappr.finance.tapprbackend.ledger.service.impl;

import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.tappr.finance.tapprbackend.ledger.data.models.LedgerEntry;
import com.tappr.finance.tapprbackend.ledger.data.repository.LedgerEntryRepository;
import com.tappr.finance.tapprbackend.ledger.service.interfaces.LedgerService;
import com.tappr.finance.tapprbackend.tapprException.TapprException;
import com.tappr.finance.tapprbackend.transaction.data.models.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    @Override
    public void debitWallet(Wallet wallet, BigDecimal amount, Transaction transaction, String description) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new TapprException("Insufficient funds");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        createEntry(wallet, amount.negate(), newBalance, transaction, description);
    }

    @Override
    public void creditWallet(Wallet wallet, BigDecimal amount, Transaction transaction, String description) {
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        createEntry(wallet, amount, newBalance, transaction, description);
    }

    private void createEntry(Wallet wallet, BigDecimal amount, BigDecimal balanceAfter, Transaction tx, String desc) {
        LedgerEntry entry = LedgerEntry.builder()
                .wallet(wallet)
                .transaction(tx)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(desc)
                .build();
        ledgerEntryRepository.save(entry);
    }
}