package com.semicolon.africa.tapprbackend.transaction.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
    private String message;
}
