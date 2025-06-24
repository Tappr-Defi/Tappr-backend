package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.semicolon.africa.tapprbackend.user.enums.Role;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor

public class LoginResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private Role role;
    private boolean loggedIn;
    private String userId;
}


