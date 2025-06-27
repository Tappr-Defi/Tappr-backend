package com.semicolon.africa.tapprbackend.Wallet.exceptions;


import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class InsufficientBalanceException extends TapprException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
