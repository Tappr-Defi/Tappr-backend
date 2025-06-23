package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateNewUserResponse {
    private String message;
    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private String phoneNumber;

}
