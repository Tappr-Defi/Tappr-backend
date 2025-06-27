package com.semicolon.africa.tapprbackend.user.controllers;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.tapprException.TapprException;
import com.semicolon.africa.tapprbackend.user.data.models.RefreshToken;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LogoutRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.semicolon.africa.tapprbackend.user.services.implementations.RefreshTokenService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<CreateNewUserResponse> register(
            @Valid @RequestBody CreateNewUserRequest request
    ) {
        try {
            log.info("Registration attempt for email: {}", request.getEmail());
            request.setEmail(request.getEmail().trim());
            request.setFirstName(request.getFirstName().trim());
            request.setLastName(request.getLastName().trim());
            request.setEmail(request.getEmail().trim());
            request.setPhoneNumber(request.getPhoneNumber().trim());
            
            CreateNewUserResponse response = authService.createNewUser(request);
            log.info("User registered successfully: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (TapprException e) {
            log.error("Registration failed for email {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration for email {}: {}", request.getEmail(), e.getMessage(), e);
            throw new TapprException("Registration failed due to an unexpected error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for email: {}", request.getEmail());
            LoginResponse response = authService.login(request);
            log.info("User logged in successfully: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (TapprException e) {
            log.error("Login failed for email {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login for email {}: {}", request.getEmail(), e.getMessage());
            throw new TapprException("Login failed due to an unexpected error");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutUserResponse> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            log.info("Logout attempt for email: {}", request.getEmail());
            LogoutUserResponse response = authService.logOut(request);
            log.info("User logged out successfully: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (TapprException e) {
            log.error("Logout failed for email {}: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during logout for email {}: {}", request.getEmail(), e.getMessage());
            throw new TapprException("Logout failed due to an unexpected error");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String token = request.get("refreshToken");

        RefreshToken refreshToken = refreshTokenService
                .findByToken(token)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        User user = refreshToken.getUser();

        refreshTokenService.revokeAllUserTokens(user);
        RefreshToken newToken = refreshTokenService.createRefreshToken(user);

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newToken.getToken()
        ));
    }
}
