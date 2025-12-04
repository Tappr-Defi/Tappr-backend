package com.tappr.finance.tapprbackend.onboarding.repositories;

import com.tappr.finance.tapprbackend.user.data.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByTokenAndUserEmail(String token, String email);
    Optional<VerificationToken> findByUserId(UUID userId);
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserEmailIgnoreCaseAndToken(String email, String token);
    void deleteAllByUserId(UUID userId);

}