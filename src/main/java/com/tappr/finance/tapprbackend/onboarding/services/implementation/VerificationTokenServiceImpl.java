package com.tappr.finance.tapprbackend.onboarding.services.implementation;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.general.messages.ErrorMessages;
import com.tappr.finance.tapprbackend.general.messages.SuccessMessages;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.VerificationTokenService;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.models.VerificationToken;
import com.tappr.finance.tapprbackend.onboarding.repositories.VerificationTokenRepository;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;

    public VerificationTokenServiceImpl(VerificationTokenRepository verificationTokenRepository, UserRepository userRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String generateToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null or must have a valid ID");
        }

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

    @Transactional
    @Override
    public ApiResponse<VerificationStatus> validateToken(String email, String otp) {

        Optional<VerificationToken> optionalToken =
                verificationTokenRepository.findByUserEmailIgnoreCaseAndToken(email, otp);

        if (optionalToken.isEmpty()) {
            return ApiResponse.failure(ErrorMessages.INVALID_TOKEN, VerificationStatus.INVALID);
        }

        VerificationToken token = optionalToken.get();

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ApiResponse.failure(ErrorMessages.TOKEN_EXPIRED, VerificationStatus.EXPIRED);
        }

        if (token.isUsed()) {
            return ApiResponse.failure(ErrorMessages.INVALID_TOKEN, VerificationStatus.INVALID);
        }

        token.setUsed(true);
        verificationTokenRepository.save(token);

        User user = token.getUser();
        user.setVerified(true);
        userRepository.save(user);

        return ApiResponse.success(SuccessMessages.EMAIL_VERIFICATION_SUCCESSFUL, VerificationStatus.ACTIVE);
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
