package com.tappr.finance.tapprbackend.onboarding.controllers;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.onboarding.dtos.requests.CreateNewUserRequest;
import com.tappr.finance.tapprbackend.onboarding.dtos.responses.CreateNewUserResponse;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.OnboardingService;
import com.tappr.finance.tapprbackend.user.dtos.requests.LoginRequest;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import com.tappr.finance.tapprbackend.user.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication (Public)", description = "Registration, Login, and Account Recovery")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UserService userService;

    // --- REGISTRATION & VERIFICATION ---

    @PostMapping("/register")
    @Operation(summary = "Register User", description = "Creates account and sends verification OTP.")
    public ResponseEntity<ApiResponse<CreateNewUserResponse>> register(@Valid @RequestBody CreateNewUserRequest request) {
        return new ResponseEntity<>(onboardingService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/verify-email-login")
    @Operation(summary = "Verify & Auto-Login", description = "Validates OTP and returns JWT tokens immediately.")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyEmailAndLogin(
            @RequestParam String email,
            @RequestParam String otp) {
        return ResponseEntity.ok(onboardingService.verifyEmailAndLogin(email, otp));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend OTP", description = "Resends OTP if user is unverified.")
    public ResponseEntity<ApiResponse<String>> resendVerification(@RequestParam String email) {
        return ResponseEntity.ok(onboardingService.resendVerificationOtp(email));
    }

    // --- LOGIN & TOKENS ---

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with Email/Phone and Password.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new Access Token using Refresh Token.")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestParam("refreshToken") String refreshToken) {
        return ResponseEntity.ok(userService.refreshAccessToken(refreshToken));
    }

    // --- PASSWORD RECOVERY ---

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password", description = "Send password reset link to email.")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(userService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Set new password using valid token.")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        return ResponseEntity.ok(userService.resetPassword(token, newPassword));
    }
}