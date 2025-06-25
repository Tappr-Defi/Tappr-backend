package com.semicolon.africa.tapprbackend.user.services.interfaces;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LogoutRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import org.springframework.stereotype.Component;

public interface AuthService {
    CreateNewUserResponse createNewUser(CreateNewUserRequest newUserRequest);
    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshAccessToken(String refreshTokenStr);

    LogoutUserResponse logOut(LogoutRequest logOutRequest);
}
