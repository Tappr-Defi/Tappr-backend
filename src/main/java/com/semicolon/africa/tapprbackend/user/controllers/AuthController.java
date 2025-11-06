package com.semicolon.africa.tapprbackend.user.controllers;

import com.semicolon.africa.tapprbackend.general.dtos.ApiResponse;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 🔹 Register a new user
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CreateNewUserResponse>> register(@Valid @RequestBody CreateNewUserRequest request) {
        ApiResponse<CreateNewUserResponse> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 🔹 Verify email and login in one step
    @PostMapping("/verify-email-login")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyEmailAndLogin(
            @RequestParam String email,
            @RequestParam String otp) {
        ApiResponse<LoginResponse> response = authService.verifyEmailAndLogin(email, otp);
        return ResponseEntity.ok(response);
    }


    // 🔹 Resend verification token
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@RequestParam String email) {
        ApiResponse<String> response = authService.resendVerificationOtp(email);
        return ResponseEntity.ok(response);
    }

    // 🔹 Login user (email or phone)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        ApiResponse<LoginResponse> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // 🔹 Refresh JWT token
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestParam("refreshToken") String refreshToken) {
        ApiResponse<LoginResponse> response = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    // 🔹 Logout user

    @PostMapping("/logout")
    public ApiResponse<LogoutUserResponse> logout() {
        return authService.logout();
    }


    // 🔹 Forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        ApiResponse<String> response = authService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    // 🔹 Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        ApiResponse<String> response = authService.resetPassword(token, newPassword);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}

