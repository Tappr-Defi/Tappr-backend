package com.semicolon.africa.tapprbackend.user.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class PasswordLenghtMismatchException extends TapprException {
    public PasswordLenghtMismatchException(String message) {
        super(message);
    }
}
