package com.semicolon.africa.tapprbackend.transaction.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(String message) {

        super(message);
    }
}
