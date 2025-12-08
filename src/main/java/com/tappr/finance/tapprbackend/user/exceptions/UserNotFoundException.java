package com.tappr.finance.tapprbackend.user.exceptions;

import com.tappr.finance.tapprbackend.tapprException.TapprException;

public class UserNotFoundException extends TapprException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
