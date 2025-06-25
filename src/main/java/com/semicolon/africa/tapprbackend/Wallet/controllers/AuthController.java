package com.semicolon.africa.tapprbackend.Wallet.controllers;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.RefreshToken;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.services.implementations.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(RefreshTokenService refreshTokenService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String token = request.get("refreshToken");

        RefreshToken refreshToken = refreshTokenService
                .findByToken(token)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        User user = refreshToken.getUser();

        // rotate: revoke old token, issue new one
        refreshTokenService.revokeAllUserTokens(user);
        RefreshToken newToken = refreshTokenService.createRefreshToken(user);

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newToken.getToken()
        ));
    }
}
