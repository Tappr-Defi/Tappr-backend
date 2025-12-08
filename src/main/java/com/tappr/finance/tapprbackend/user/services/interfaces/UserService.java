package com.tappr.finance.tapprbackend.user.services.interfaces;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.kyc.dtos.requests.IdVerificationRequest;
import com.tappr.finance.tapprbackend.kyc.dtos.responses.KycProviderResponse;
import com.tappr.finance.tapprbackend.user.dtos.requests.LoginRequest;
import com.tappr.finance.tapprbackend.user.dtos.requests.ProfileSetupRequest;
import com.tappr.finance.tapprbackend.user.dtos.responses.LoginResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.ProfileSetupResponse;
import jakarta.transaction.Transactional;

public interface UserService {

    @Transactional
    ApiResponse<LoginResponse> login(LoginRequest request);
    ApiResponse<LoginResponse> refreshAccessToken(String refreshToken);
    ApiResponse<LogoutUserResponse> logout();
    ApiResponse<String> forgotPassword(String email);

    @Transactional
    ApiResponse<String> resendVerificationOtp(String email);

    @Transactional
    ApiResponse<String> resetPassword(String token, String newPassword);

    ApiResponse<ProfileSetupResponse> setupProfile(ProfileSetupRequest request);
    ApiResponse<KycProviderResponse> startKycTier1(IdVerificationRequest request);
}
