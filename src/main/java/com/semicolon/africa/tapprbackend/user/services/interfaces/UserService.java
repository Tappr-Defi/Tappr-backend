package com.semicolon.africa.tapprbackend.user.services.interfaces;

import com.semicolon.africa.tapprbackend.general.dtos.ApiResponse;
import com.semicolon.africa.tapprbackend.general.enums.VerificationStatus;
import com.semicolon.africa.tapprbackend.onboarding.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.onboarding.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import jakarta.transaction.Transactional;

public interface UserService {
    @Transactional
    ApiResponse<CreateNewUserResponse> register(CreateNewUserRequest request);

    ApiResponse<VerificationStatus> validateToken(String email, String otp);

    @Transactional
    ApiResponse<LoginResponse> verifyEmailAndLogin(String email, String otp);

    @Transactional
    ApiResponse<String> resendVerificationOtp(String email);

    @Transactional
    ApiResponse<LoginResponse> login(LoginRequest request);

    ApiResponse<LoginResponse> refreshAccessToken(String refreshToken);

    ApiResponse<LogoutUserResponse> logout();

    ApiResponse<String> forgotPassword(String email);

    @Transactional
    ApiResponse<String> resetPassword(String token, String newPassword);
}
