package com.semicolon.africa.tapprbackend.user.services.interfaces;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import org.springframework.stereotype.Component;

@Component
public interface UserService {

    LoginResponse generateLoginResponse(User user, String message);
}
