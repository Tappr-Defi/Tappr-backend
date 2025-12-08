package com.tappr.finance.tapprbackend.kyc.dtos.responses;

import com.tappr.finance.tapprbackend.kyc.enums.KycStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KycProviderResponse {
    private boolean isSuccess;
    private KycStatus status;
    private String message;
}
