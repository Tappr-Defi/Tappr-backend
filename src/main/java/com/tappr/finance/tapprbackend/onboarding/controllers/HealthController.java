package com.tappr.finance.tapprbackend.onboarding.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> checkHealth() {
        return ResponseEntity.ok("Tappr Backend is Running 🚀");
    }
}