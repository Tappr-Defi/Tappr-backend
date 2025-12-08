package com.tappr.finance.tapprbackend.onboarding.services.interfaces;


import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.general.enums.VerificationStatus;
import com.tappr.finance.tapprbackend.user.data.models.User;
import jakarta.transaction.Transactional;

public interface VerificationTokenService {
    String generateToken(User user);

    @Transactional
    ApiResponse<VerificationStatus> validateToken(String email, String otp);
    VerificationStatus validateToken(String token);
}
