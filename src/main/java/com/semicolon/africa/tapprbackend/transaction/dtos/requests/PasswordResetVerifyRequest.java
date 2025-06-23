package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetVerifyRequest {
    private String token;
    private String userName;
    private String newPassword;
}
