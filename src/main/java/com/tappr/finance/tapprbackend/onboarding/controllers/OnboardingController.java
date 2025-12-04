package com.tappr.finance.tapprbackend.onboarding.controllers;


import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.onboarding.dtos.requests.CreateNewUserRequest;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.OnboardingService;
import com.tappr.finance.tapprbackend.user.dtos.requests.LoginRequest;
import com.tappr.finance.tapprbackend.onboarding.dtos.responses.CreateNewUserResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.tappr.finance.tapprbackend.user.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

// 🔹 Register a new user
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CreateNewUserResponse>> register(@Valid @RequestBody CreateNewUserRequest request) {
        ApiResponse<CreateNewUserResponse> response = onboardingService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 🔹 Verify email and login in one step
    @PostMapping("/verify-email-login")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyEmailAndLogin(
            @RequestParam String email,
            @RequestParam String otp) {
        ApiResponse<LoginResponse> response = onboardingService.verifyEmailAndLogin(email, otp);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/validate-otp")
    public ResponseEntity<ApiResponse<VerificationStatus>> validateToken(@RequestParam String email, String otp){
        ApiResponse<VerificationStatus> response = onboardingService.validateToken(email, otp);
        return ResponseEntity.ok(response);
    }


}