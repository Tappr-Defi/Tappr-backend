package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantProfileResponse {
    private UUID id;
    private String businessName;
    private String businessType;
    private String bankAccountNumber;
    private String bankName;
    private String deviceId;
    private UUID userId;
    private String userEmail;
    private String userFullName;
}