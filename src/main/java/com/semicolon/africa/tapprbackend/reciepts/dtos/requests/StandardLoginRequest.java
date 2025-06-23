package com.semicolon.africa.tapprbackend.reciepts.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.vomzersocials.user.enums.LoginMethod;



@Data
public class StandardLoginRequest {
    @NotEmpty(message = "Username cannot be empty")
    private String userName;

    @NotEmpty(message = "Password cannot be empty")
    private String password;

    private LoginMethod loginMethod = LoginMethod.STANDARD_LOGIN;
}
