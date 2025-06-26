package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.*;
import com.semicolon.africa.tapprbackend.user.enums.Role;

@Data
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


