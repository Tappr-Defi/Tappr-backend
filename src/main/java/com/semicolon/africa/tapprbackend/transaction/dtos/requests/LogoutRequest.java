package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogoutRequest {
    @NotBlank
    private String username;
    private String refreshToken;
}
