package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.dtos.requests.NewUserRequest;
import com.semicolon.africa.tapprbackend.user.services.interfaces.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public User createNewUser(NewUserRequest newUserRequest) {
        return null;
    }
}
