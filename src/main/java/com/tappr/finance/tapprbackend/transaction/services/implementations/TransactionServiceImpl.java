package com.tappr.finance.tapprbackend.transaction.services.implementations;

import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.messages.ErrorMessages;
import com.tappr.finance.tapprbackend.general.messages.SuccessMessages;
import com.tappr.finance.tapprbackend.ledger.service.interfaces.LedgerService;
import com.tappr.finance.tapprbackend.reciepts.services.interfaces.ReceiptService;
import com.tappr.finance.tapprbackend.tapprException.TapprException;
import com.tappr.finance.tapprbackend.transaction.data.models.Transaction;
import com.tappr.finance.tapprbackend.transaction.data.repositories.TransactionRepository;
import com.tappr.finance.tapprbackend.transaction.dtos.requests.DepositFundRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.requests.TransferRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.responses.TransactionResponse;
import com.tappr.finance.tapprbackend.transaction.enums.TransactionStatus;
import com.tappr.finance.tapprbackend.transaction.enums.TransactionType;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import com.tappr.finance.tapprbackend.transaction.services.interfaces.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerService ledgerService;
    private final ReceiptService receiptService;

    @Transactional
    @Override
    public ApiResponse<TransactionResponse> transferFunds(TransferRequest request) {
        try {
            String senderId = SecurityContextHolder.getContext().getAuthentication().getName();
            User sender = userRepository.findById(UUID.fromString(senderId))
                    .orElseThrow(() -> new TapprException(ErrorMessages.USER_NOT_FOUND));

            User recipient = userRepository.findByEmailIgnoreCase(request.getRecipientIdentifier())
                    .or(() -> userRepository.findByPhoneNumber(request.getRecipientIdentifier()))
                    .orElseThrow(() -> new TapprException("Recipient not found"));

            if (sender.getId().equals(recipient.getId())) {
                throw new TapprException("Cannot transfer to self");
            }

            Wallet sourceWallet = walletRepository.findByUserAndCurrencyCode(sender, request.getCurrencyCode())
                    .orElseThrow(() -> new TapprException("Sender wallet not found"));

            Wallet destWallet = walletRepository.findByUserAndCurrencyCode(recipient, request.getCurrencyCode())
                    .orElseThrow(() -> new TapprException("Recipient wallet not found"));

            Transaction tx = Transaction.builder()
                    .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .sourceWallet(sourceWallet)
                    .destinationWallet(destWallet)
                    .amount(request.getAmount())
                    .currencyCode(request.getCurrencyCode())
                    .type(TransactionType.TRANSFER)
                    .status(TransactionStatus.SUCCESS)
                    .description(request.getDescription())
                    .initiatedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(tx);

            ledgerService.debitWallet(
                    sourceWallet,
                    request.getAmount(),
                    tx,
                    "Transfer to " + recipient.getUsername()
            );

            ledgerService.creditWallet(
                    destWallet,
                    request.getAmount(),
                    tx,
                    "Transfer from " + sender.getUsername()
            );

            TransactionResponse response = new TransactionResponse();
            response.setTransactionRef(tx.getTransactionRef());
            response.setAmount(tx.getAmount());
            response.setStatus(tx.getStatus().name());
            response.setMessage("Transfer successful");

            return ApiResponse.success("Transfer successful", response);

        } catch (TapprException ex) {
            return ApiResponse.failure(ex.getMessage());
        } catch (Exception ex) {
            log.error("Transfer error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    @Transactional
    @Override
    public ApiResponse<TransactionResponse> depositFunds(DepositFundRequest request) {
        try{
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new TapprException("Wallet not found"));

            Wallet wallet = walletRepository.findByUserAndCurrencyCode(user, request.getCurrencyCode())
                    .orElseThrow(() -> new TapprException(ErrorMessages.USER_NOT_FOUND));
            Transaction tx = Transaction.builder()
                    .transactionRef("DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .destinationWallet(wallet)
                    .sourceWallet(null)
                    .amount(request.getAmount())
                    .currencyCode(request.getCurrencyCode())
                    .type(TransactionType.DEPOSIT)
                    .status(TransactionStatus.SUCCESS)
                    .description("Deposit via " + request.getPaymentMethod())
                    .initiatedAt(LocalDateTime.now())
                    .completedAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(tx);

            ledgerService.creditWallet(wallet, request.getAmount(), tx, "Deposit via " + request.getPaymentMethod());

            receiptService.generateReceipt(tx);
            return ApiResponse.success("Deposit successful", mapToTransactionResponse(tx));

        } catch (Exception ex) {
            log.error("Deposit error: ", ex);
            return ApiResponse.failure(ErrorMessages.OPERATION_FAILED);
        }
    }

    private TransactionResponse mapToTransactionResponse(Transaction tx) {
        TransactionResponse response = new TransactionResponse();
        response.setAmount(tx.getAmount());
        response.setTransactionRef(tx.getTransactionRef());
        response.setMessage(SuccessMessages.TRANSACTION_SUCCESSFUL);
        response.setStatus(TransactionStatus.SUCCESS.toString());

        return response;
    }
}