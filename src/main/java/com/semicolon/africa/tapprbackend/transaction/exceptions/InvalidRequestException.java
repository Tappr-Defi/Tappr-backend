package com.semicolon.africa.tapprbackend.transaction.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class InvalidRequestException extends TapprException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
