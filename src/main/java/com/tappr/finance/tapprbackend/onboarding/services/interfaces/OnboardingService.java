package com.tappr.finance.tapprbackend.onboarding.services.interfaces;


import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.onboarding.dtos.requests.CreateNewUserRequest;
import com.tappr.finance.tapprbackend.onboarding.dtos.responses.CreateNewUserResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import jakarta.transaction.Transactional;

public interface OnboardingService {
    ApiResponse<CreateNewUserResponse> register(CreateNewUserRequest request);

    @Transactional // Ensure this method is transactional for saving both token and user
    ApiResponse<VerificationStatus> validateToken(String email, String otp);

    @Transactional
    ApiResponse<LoginResponse> verifyEmailAndLogin(String email, String otp);

    ApiResponse<String> resendVerificationOtp(String email);
}