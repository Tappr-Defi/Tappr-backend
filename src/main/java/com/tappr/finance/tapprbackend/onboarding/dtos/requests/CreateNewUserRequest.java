package com.tappr.finance.tapprbackend.onboarding.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class CreateNewUserRequest {
//

    @NotEmpty(message = "phone number cannot be empty")
    private String phoneNumber;

    @NotEmpty(message = "email address cannot be empty")
    private String email;

    @NotEmpty(message = "password cannot be empty")
    private String password;

}
