package com.tappr.finance.tapprbackend.user.controllers;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.user.dtos.requests.LoginRequest;
import com.tappr.finance.tapprbackend.user.dtos.requests.ProfileSetupRequest;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.ProfileSetupResponse;
import com.tappr.finance.tapprbackend.user.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // 🔹 Resend verification token
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@RequestParam String email) {
        ApiResponse<String> response = userService.resendVerificationOtp(email);
        return ResponseEntity.ok(response);
    }

    // 🔹 Login user (email or phone)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        ApiResponse<LoginResponse> response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // 🔹 Refresh JWT token
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestParam("refreshToken") String refreshToken) {
        ApiResponse<LoginResponse> response = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    // 🔹 Logout user
    @PostMapping("/logout")
    public ApiResponse<LogoutUserResponse> logout() {
        return userService.logout();
    }


    // 🔹 Forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        ApiResponse<String> response = userService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    // 🔹 Reset password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        ApiResponse<String> response = userService.resetPassword(token, newPassword);
        return new ResponseEntity<>(response, response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    // 🔹 Setup profile password
    @PostMapping("/setup-profile")
    public ResponseEntity<ApiResponse<ProfileSetupResponse>> setupProfile(@RequestBody ProfileSetupRequest profileSetupRequest){
        ApiResponse<ProfileSetupResponse> response = userService.setupProfile(profileSetupRequest);
        return ResponseEntity.ok(response);
    }

}

