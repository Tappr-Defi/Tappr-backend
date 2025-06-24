package com.semicolon.africa.tapprbackend.user.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class UserNotFoundException extends TapprException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
