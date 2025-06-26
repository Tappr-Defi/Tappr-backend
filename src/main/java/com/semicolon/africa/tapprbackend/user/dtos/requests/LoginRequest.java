package com.semicolon.africa.tapprbackend.user.dtos.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request object for user login")
public class LoginRequest {
    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    private String email;
    
    @Schema(description = "User's password", example = "securePassword123", required = true)
    private String password;
}
