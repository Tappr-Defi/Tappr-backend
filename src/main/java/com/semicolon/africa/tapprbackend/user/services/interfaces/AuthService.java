package com.semicolon.africa.tapprbackend.user.services.interfaces;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import org.springframework.stereotype.Component;

public interface AuthService {
    CreateNewUserResponse createNewUser(CreateNewUserRequest newUserRequest);

}
