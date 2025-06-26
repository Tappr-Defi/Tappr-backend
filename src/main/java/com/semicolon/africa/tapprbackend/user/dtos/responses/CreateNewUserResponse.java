package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CreateNewUserResponse {
    private String message;
    private String userId;
    private String email;
    private String phoneNumber;


}
