package com.semicolon.africa.tapprbackend.user.controllers;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.tapprException.TapprException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                throw new TapprException("Authorization token is required");
            }

            if (!jwtUtil.validateToken(token)) {
                throw new TapprException("Invalid or expired token");
            }

            String email = jwtUtil.extractEmail(token);
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                throw new TapprException("User not found");
            }

            User user = userOptional.get();
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("email", user.getEmail());
            profile.put("phoneNumber", user.getPhoneNumber());
            profile.put("role", user.getRole());
            profile.put("kycVerified", user.isKycVerified());
            profile.put("createdAt", user.getCreatedAt());

            log.info("Profile retrieved for user: {}", email);
            return ResponseEntity.ok(profile);

        } catch (TapprException e) {
            log.error("Error retrieving user profile: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error retrieving user profile: {}", e.getMessage());
            throw new TapprException("Failed to retrieve user profile");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                throw new TapprException("Refresh token is required");
            }

            if (!jwtUtil.validateToken(token)) {
                throw new TapprException("Invalid or expired refresh token");
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            
            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(email, 
                com.semicolon.africa.tapprbackend.user.enums.Role.valueOf(role));

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("message", "Token refreshed successfully");

            log.info("Token refreshed for user: {}", email);
            return ResponseEntity.ok(response);

        } catch (TapprException e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error refreshing token: {}", e.getMessage());
            throw new TapprException("Failed to refresh token");
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                throw new TapprException("Authorization token is required");
            }

            boolean isValid = jwtUtil.validateToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                response.put("email", jwtUtil.extractEmail(token));
                response.put("role", jwtUtil.extractRole(token));
            }

            return ResponseEntity.ok(response);

        } catch (TapprException e) {
            log.error("Error validating token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", "Token validation failed");
            return ResponseEntity.ok(response);
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}