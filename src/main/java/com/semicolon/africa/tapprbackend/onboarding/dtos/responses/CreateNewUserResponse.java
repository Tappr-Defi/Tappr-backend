package com.semicolon.africa.tapprbackend.onboarding.dtos.responses;

import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
public class CreateNewUserResponse {
    private String message;
    private String userId;
    private String email;
    private String phoneNumber;
}
