package com.tappr.finance.tapprbackend.kyc.services.interfaces;

import com.tappr.finance.tapprbackend.kyc.dtos.requests.IdVerificationRequest;
import com.tappr.finance.tapprbackend.kyc.dtos.responses.KycProviderResponse;

import java.util.UUID;

public interface KycProviderService {
    KycProviderResponse submitVerification(UUID userId, IdVerificationRequest request);
}
