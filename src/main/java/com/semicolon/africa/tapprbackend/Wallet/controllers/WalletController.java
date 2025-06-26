package com.semicolon.africa.tapprbackend.Wallet.controllers;

import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.CreateWalletResponse;
import com.semicolon.africa.tapprbackend.Wallet.service.interfaces.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<CreateWalletResponse> createWallet(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateWalletRequest request) {
        String token = authHeader.replace("Bearer ", "");
        CreateWalletResponse response = walletService.createWalletForUser(token, request);
        return ResponseEntity.ok(response);
    }
}
