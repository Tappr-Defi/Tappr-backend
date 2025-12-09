package com.tappr.finance.tapprbackend.transaction.controllers;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.transaction.dtos.requests.TransferRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.responses.TransactionResponse;
import com.tappr.finance.tapprbackend.transaction.services.interfaces.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "2. Transactions", description = "Endpoints for money movement (P2P, Deposit, Withdrawal)")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "P2P Transfer", description = "Send money instantly to another Tappr user (Internal Transfer).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transfer Successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient Funds / Invalid Currency / Self-Transfer attempted",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Recipient User or Wallet not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> transferFunds(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transactionService.transferFunds(request));
    }
}