package com.semicolon.africa.tapprbackend.Wallet.exceptions;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;

public class WalletAlreadyExistsException extends TapprException {
    public WalletAlreadyExistsException(String message) {
        super(message);
    }
}
