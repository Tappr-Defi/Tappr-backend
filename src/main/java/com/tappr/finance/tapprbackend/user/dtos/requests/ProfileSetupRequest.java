package com.tappr.finance.tapprbackend.user.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Setter
@Getter
public class ProfileSetupRequest {
    @NotEmpty(message = "first name cannot be empty")
    private String firstName;

    @NotEmpty(message = "last name cannot be empty")
    private String lastName;

    private Optional<String> username;
}
