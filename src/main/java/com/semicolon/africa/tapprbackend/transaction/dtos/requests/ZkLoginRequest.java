package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class ZkLoginRequest {
//    @NotEmpty(message = "Username cannot be empty")
//    private String userName;

    @NotEmpty(message = "Public key cannot be empty")
    private String jwt;

}

