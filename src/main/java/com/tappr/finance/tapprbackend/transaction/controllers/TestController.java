package com.tappr.finance.tapprbackend.transaction.controllers;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.transaction.dtos.requests.DepositFundRequest;
import com.tappr.finance.tapprbackend.transaction.dtos.responses.TransactionResponse;
import com.tappr.finance.tapprbackend.transaction.services.interfaces.TransactionService;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/test-txn")
@RequiredArgsConstructor
@Tag(name = "Testing Utilities", description = "Endpoints for simulating scenarios (Dev Only)")
public class TestController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping("/deposit")
    @Operation(summary = "Simulate Deposit", description = "Add fake money to the logged-in user's wallet.")
    public ResponseEntity<ApiResponse<TransactionResponse>> fundWallet(@RequestBody DepositFundRequest request) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok(
                transactionService.depositFunds(request)
        );
    }

}