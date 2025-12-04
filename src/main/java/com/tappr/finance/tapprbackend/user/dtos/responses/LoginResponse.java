package com.tappr.finance.tapprbackend.user.dtos.responses;

import lombok.*;
import com.tappr.finance.tapprbackend.user.enums.Role;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder

public class LoginResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private Role role;
    private boolean loggedIn;
    private String userId;
}


