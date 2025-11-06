package com.semicolon.africa.tapprbackend.user.data.repositories;

import com.semicolon.africa.tapprbackend.user.data.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
}
