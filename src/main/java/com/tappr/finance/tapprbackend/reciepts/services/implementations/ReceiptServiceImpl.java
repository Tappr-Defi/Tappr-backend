package com.tappr.finance.tapprbackend.reciepts.services.implementations;


import com.tappr.finance.tapprbackend.reciepts.data.models.Receipt;
import com.tappr.finance.tapprbackend.reciepts.data.repositories.ReceiptRepository;
import com.tappr.finance.tapprbackend.reciepts.dtos.responses.ReceiptCreationResponse;
import com.tappr.finance.tapprbackend.reciepts.services.interfaces.ReceiptService;
import com.tappr.finance.tapprbackend.transaction.data.models.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;

    @Transactional
    @Override
    public Receipt generateReceipt(Transaction transaction) {
        // 1. Generate Unique Receipt Number
        String receiptNo = "RCP-" + System.currentTimeMillis() + "-" + transaction.getTransactionRef().substring(4, 8);

        // 2. Create Receipt Entity
        Receipt receipt = Receipt.builder()
                .transaction(transaction)
                .receiptNumber(receiptNo)
                .senderName(transaction.getSourceWallet().getUser().getFullName())
                .recipientName(transaction.getDestinationWallet().getUser().getFullName())
                .senderAccountMasked(maskAccount(transaction.getSourceWallet().getAccountNumber()))
                .recipientAccountMasked(maskAccount(transaction.getDestinationWallet().getAccountNumber()))
                .transactionFee("10.00 NGN") // Logic to calculate fee goes here
                .loyaltyPointsEarned("50 Tappr") // Logic to fetch loyalty points goes here
                .build();

        return receiptRepository.save(receipt);
    }

    @Override
    public ReceiptCreationResponse getReceiptDetails(UUID transactionId) {
        Receipt receipt = receiptRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Receipt not found"));

        return mapToResponse(receipt);
    }

    private String maskAccount(String account) {
        if (account == null || account.length() < 4) return "****";
        return "**** " + account.substring(account.length() - 4);
    }

    private ReceiptCreationResponse mapToResponse(Receipt receipt) {
        return ReceiptCreationResponse.builder()
                .receiptNumber(receipt.getReceiptNumber())
                .amountSent(receipt.getTransaction().getAmount())
                .currency(receipt.getTransaction().getCurrencyCode())
                .senderName(receipt.getSenderName())
                .recipientName(receipt.getRecipientName())
                .recipientTimestamp(receipt.getGeneratedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm a")))
                .status("Success")
                .build();
    }
}