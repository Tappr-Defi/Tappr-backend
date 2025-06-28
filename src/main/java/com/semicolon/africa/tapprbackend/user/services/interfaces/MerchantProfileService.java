package com.semicolon.africa.tapprbackend.user.services.interfaces;

import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateMerchantProfileRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.MerchantProfileResponse;

import java.util.UUID;

public interface MerchantProfileService {
    
    /**
     * Create a merchant profile for a user and upgrade their role to MERCHANT
     * @param userId The user ID
     * @param request The merchant profile creation request
     * @return The created merchant profile response
     */
    MerchantProfileResponse createMerchantProfile(UUID userId, CreateMerchantProfileRequest request);
    
    /**
     * Get merchant profile by user ID
     * @param userId The user ID
     * @return The merchant profile response
     */
    MerchantProfileResponse getMerchantProfile(UUID userId);
    
    /**
     * Update merchant profile
     * @param userId The user ID
     * @param request The merchant profile update request
     * @return The updated merchant profile response
     */
    MerchantProfileResponse updateMerchantProfile(UUID userId, CreateMerchantProfileRequest request);
    
    /**
     * Check if user has merchant profile
     * @param userId The user ID
     * @return true if user has merchant profile, false otherwise
     */
    boolean hasMerchantProfile(UUID userId);
    
    /**
     * Deactivate merchant profile (downgrade to REGULAR user)
     * @param userId The user ID
     */
    void deactivateMerchantProfile(UUID userId);
}