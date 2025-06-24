package com.semicolon.africa.tapprbackend.transaction.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class MerchantNotFoundException extends TapprException {
    public MerchantNotFoundException(String message) {

        super(message);
    }
}
