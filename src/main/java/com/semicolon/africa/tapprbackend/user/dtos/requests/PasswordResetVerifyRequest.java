package com.semicolon.africa.tapprbackend.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetVerifyRequest {
    private String token;
    private String userName;
    private String newPassword;
}
