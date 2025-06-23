package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
    private String message;
}
