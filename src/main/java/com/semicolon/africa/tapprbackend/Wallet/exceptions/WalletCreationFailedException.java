package com.semicolon.africa.tapprbackend.Wallet.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class WalletCreationFailedException extends TapprException {
    public WalletCreationFailedException(String message) {
        super(message);
    }
}
