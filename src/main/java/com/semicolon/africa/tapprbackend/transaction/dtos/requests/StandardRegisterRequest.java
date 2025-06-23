package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class StandardRegisterRequest {
    @NotEmpty(message = "Username cannot be empty")
    private String userName;

    @NotEmpty(message = "Password cannot be empty")
    private String password;
}

