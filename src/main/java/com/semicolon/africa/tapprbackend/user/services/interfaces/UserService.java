package com.semicolon.africa.tapprbackend.user.services.interfaces;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.dtos.requests.NewUserRequest;
import org.springframework.stereotype.Component;

@Component
public interface UserService {
    User createNewUser(NewUserRequest newUserRequest);
}
