package com.tappr.finance.tapprbackend.reciepts.services.interfaces;

import com.tappr.finance.tapprbackend.reciepts.data.models.Receipt;
import com.tappr.finance.tapprbackend.reciepts.dtos.responses.ReceiptCreationResponse;
import com.tappr.finance.tapprbackend.transaction.data.models.Transaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ReceiptService {
    @Transactional
    Receipt generateReceipt(Transaction transaction);

    ReceiptCreationResponse getReceiptDetails(UUID transactionId);
}
