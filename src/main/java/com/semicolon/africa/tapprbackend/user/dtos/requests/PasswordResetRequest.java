package com.semicolon.africa.tapprbackend.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordResetRequest {
    private String userName;
}
