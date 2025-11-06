package com.semicolon.africa.tapprbackend.user.services.interfaces;


import com.semicolon.africa.tapprbackend.general.enums.VerificationStatus;
import com.semicolon.africa.tapprbackend.user.data.models.User;

public interface VerificationTokenService {
    String generateToken(User user);

    VerificationStatus validateToken(String token);

}
