package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.general.enums.VerificationStatus;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.models.VerificationToken;
import com.semicolon.africa.tapprbackend.user.data.repositories.VerificationTokenRepository;
import com.semicolon.africa.tapprbackend.user.services.interfaces.VerificationTokenService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final SecureRandom random = new SecureRandom();

    public VerificationTokenServiceImpl(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Override
    public String generateToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null or must have a valid ID");
        }

        // Generate a 6-digit numeric OTP
        String token = String.format("%06d", random.nextInt(1_000_000));

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        verificationToken.setCreatedAt(LocalDateTime.now());
        verificationToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        verificationTokenRepository.deleteAllByUserId(user.getId());
        verificationTokenRepository.save(verificationToken);

        return token;
    }

    @Override
    public VerificationStatus validateToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .map(verificationToken -> {
                    if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return VerificationStatus.EXPIRED;
                    }
                    return VerificationStatus.ACTIVE;
                })
                .orElse(VerificationStatus.INVALID);
    }
}
