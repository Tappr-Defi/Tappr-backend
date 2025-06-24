package com.semicolon.africa.tapprbackend.transaction.controllers;

import com.semicolon.africa.tapprbackend.transaction.dtos.requests.CreateTransactionRequest;
import com.semicolon.africa.tapprbackend.transaction.dtos.responses.CreateTransactionResponse;
import com.semicolon.africa.tapprbackend.transaction.services.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<CreateTransactionResponse> createTransaction(@RequestBody CreateTransactionRequest request) {
        CreateTransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
