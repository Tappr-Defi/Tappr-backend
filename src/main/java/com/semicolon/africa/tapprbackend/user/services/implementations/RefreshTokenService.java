package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.RefreshToken;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    
    @PersistenceContext
    private EntityManager entityManager;

    public RefreshTokenService(RefreshTokenRepository repo, JwtUtil jwtUtil) {
        this.refreshTokenRepository = repo;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        String signedJwtToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId(), user.getRole());
        token.setToken(signedJwtToken);
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now()) || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new IllegalStateException("Refresh token is expired or revoked");
        }

        if (!jwtUtil.validateRefreshToken(token.getToken())) {
            refreshTokenRepository.delete(token);
            throw new IllegalStateException("Invalid refresh token signature");
        }

        return token;
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        entityManager.flush(); // Force the delete to be executed immediately
    }

    public Optional<RefreshToken> findByToken(String refreshTokenStr) {
        return refreshTokenRepository.findByToken(refreshTokenStr);
    }
}