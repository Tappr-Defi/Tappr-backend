package com.semicolon.africa.tapprbackend.transaction.controllers;

import com.semicolon.africa.tapprbackend.transaction.services.interfaces.SuiRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final SuiRateService suiRateService;

    @GetMapping("/sui-ngn")
    public ResponseEntity<Map<String, Object>> getSuiNgnRate() {
        BigDecimal rate = suiRateService.getSuiToNgnRate();
        Map<String, Object> response = new HashMap<>();
        response.put("rate", rate);
        response.put("pair", "SUI/NGN");
        return ResponseEntity.ok(response);
    }
}
