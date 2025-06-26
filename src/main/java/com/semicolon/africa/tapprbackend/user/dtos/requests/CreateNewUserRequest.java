package com.semicolon.africa.tapprbackend.user.dtos.requests;

import com.semicolon.africa.tapprbackend.user.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
@Schema(description = "Request object for user registration")
public class CreateNewUserRequest {
    @Schema(description = "User's first name", example = "John", required = true)
    @NotEmpty(message = "first name cannot be empty")
    @Pattern(
            regexp = "^[a-zA-ZÀ-ÖØ-öø-ÿ'\\- ]+$",
            message = "First name contains invalid characters"
    )
    private String firstName;

    @Schema(description = "User's last name", example = "Doe", required = true)
    @NotEmpty(message = "last name cannot be empty")
    @Pattern(
            regexp = "^[a-zA-ZÀ-ÖØ-öø-ÿ'\\- ]+$",
            message = "Last name contains invalid characters"
    )
    private String lastName;

    @Schema(description = "User's phone number in international format", example = "+1234567890", required = true)
    @Pattern(regexp = "^\\+?[0-9\\s]{7,14}$",
            message = "Invalid phone number format")
    private String phoneNumber;

    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "User's password", example = "securePassword123", required = true)
    @NotEmpty(message = "password cannot be empty")
    private String password;

}
