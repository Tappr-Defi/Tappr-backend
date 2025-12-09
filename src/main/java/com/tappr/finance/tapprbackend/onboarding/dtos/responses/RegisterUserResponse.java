package com.tappr.finance.tapprbackend.onboarding.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {
    private boolean success;
    private String message;
    private String suiAddress;
    private String username;
    private int statusCode;
    private Map<String, Object> responseBody;
}