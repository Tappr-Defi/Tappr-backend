package com.semicolon.africa.tapprbackend.Wallet.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class WalletNotFoundException extends TapprException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}
