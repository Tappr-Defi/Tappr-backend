package com.semicolon.africa.tapprbackend.user.controllers;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.tapprException.TapprException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "User Management", description = "User profile and token management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Operation(
            summary = "Get user profile",
            description = "Retrieves the authenticated user's profile information including personal details and account status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "User Profile Response",
                                    value = """
                                    {
                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                        "firstName": "John",
                                        "lastName": "Doe",
                                        "email": "john.doe@example.com",
                                        "phoneNumber": "+2348012345678",
                                        "role": "REGULAR",
                                        "kycVerified": false,
                                        "createdAt": "2024-01-15T10:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @Parameter(hidden = true) HttpServletRequest request) {
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

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using the current valid token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Token Refresh Response",
                                    value = """
                                    {
                                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                        "message": "Token refreshed successfully"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(
            @Parameter(hidden = true) HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                throw new TapprException("Refresh token is required");
            }

            if (!jwtUtil.validateToken(token)) {
                throw new TapprException("Invalid or expired refresh token");
            }

            String email = jwtUtil.extractEmail(token);
            String userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);
            
            String newAccessToken = jwtUtil.generateToken(email,
                UUID.fromString(userId),
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

    @Operation(
            summary = "Validate JWT token",
            description = "Validates the provided JWT token and returns token information if valid"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token validation result",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Valid Token Response",
                                            value = """
                                            {
                                                "valid": true,
                                                "email": "john.doe@example.com",
                                                "role": "REGULAR"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Token Response",
                                            value = """
                                            {
                                                "valid": false,
                                                "error": "Token validation failed"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(
            @Parameter(hidden = true) HttpServletRequest request) {
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