package com.tappr.finance.tapprbackend.kyc.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IdVerificationRequest {
    private String idType;     // e.g., "NIN", "Passport", "Voter's Card"
    private String idNumber;
    private String selfieImageBase64;
}
