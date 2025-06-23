package com.semicolon.africa.tapprbackend.user.dtos.requests;

import com.semicolon.africa.tapprbackend.user.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class CreateNewUserRequest {
    @NotEmpty(message = "first name cannot be empty")
    private String firstName;

    @NotEmpty(message = "last name cannot be empty")
    private String lastName;

    @NotEmpty(message = "phone number cannot be empty")
    private String phoneNumber;

    @NotEmpty(message = "email address cannot be empty")
    private String email;

    @NotEmpty(message = "password cannot be empty")
    private String password;

}
