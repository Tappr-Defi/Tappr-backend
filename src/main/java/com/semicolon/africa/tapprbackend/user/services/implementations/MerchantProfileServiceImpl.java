package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.tapprException.TapprException;
import com.semicolon.africa.tapprbackend.user.data.models.MerchantProfile;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.MerchantProfileRepository;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateMerchantProfileRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.MerchantProfileResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.services.interfaces.MerchantProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantProfileServiceImpl implements MerchantProfileService {
    
    private final MerchantProfileRepository merchantProfileRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public MerchantProfileResponse createMerchantProfile(UUID userId, CreateMerchantProfileRequest request) {
        log.info("Creating merchant profile for user ID: {}", userId);
        
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TapprException("User not found"));
        
        // Check if user already has a merchant profile
        if (merchantProfileRepository.existsByUser(user)) {
            throw new TapprException("User already has a merchant profile");
        }
        
        // Create merchant profile
        MerchantProfile merchantProfile = new MerchantProfile();
        merchantProfile.setUser(user);
        merchantProfile.setBusinessName(request.getBusinessName());
        merchantProfile.setBusinessType(request.getBusinessType());
        merchantProfile.setBankAccountNumber(request.getBankAccountNumber());
        merchantProfile.setBankName(request.getBankName());
        merchantProfile.setDeviceId(request.getDeviceId());
        
        // Save merchant profile
        MerchantProfile savedProfile = merchantProfileRepository.save(merchantProfile);
        
        // Update user role to MERCHANT
        user.setRole(Role.MERCHANT);
        user.setMerchantProfile(savedProfile);
        userRepository.save(user);
        
        log.info("Merchant profile created successfully for user: {}", user.getEmail());
        
        return mapToResponse(savedProfile);
    }
    
    @Override
    public MerchantProfileResponse getMerchantProfile(UUID userId) {
        log.info("Getting merchant profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TapprException("User not found"));
        
        MerchantProfile merchantProfile = merchantProfileRepository.findByUser(user)
                .orElseThrow(() -> new TapprException("Merchant profile not found"));
        
        return mapToResponse(merchantProfile);
    }
    
    @Override
    @Transactional
    public MerchantProfileResponse updateMerchantProfile(UUID userId, CreateMerchantProfileRequest request) {
        log.info("Updating merchant profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TapprException("User not found"));
        
        MerchantProfile merchantProfile = merchantProfileRepository.findByUser(user)
                .orElseThrow(() -> new TapprException("Merchant profile not found"));
        
        // Update merchant profile fields
        merchantProfile.setBusinessName(request.getBusinessName());
        merchantProfile.setBusinessType(request.getBusinessType());
        merchantProfile.setBankAccountNumber(request.getBankAccountNumber());
        merchantProfile.setBankName(request.getBankName());
        merchantProfile.setDeviceId(request.getDeviceId());
        
        MerchantProfile updatedProfile = merchantProfileRepository.save(merchantProfile);
        
        log.info("Merchant profile updated successfully for user: {}", user.getEmail());
        
        return mapToResponse(updatedProfile);
    }
    
    @Override
    public boolean hasMerchantProfile(UUID userId) {
        return merchantProfileRepository.existsByUserId(userId);
    }
    
    @Override
    @Transactional
    public void deactivateMerchantProfile(UUID userId) {
        log.info("Deactivating merchant profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TapprException("User not found"));
        
        MerchantProfile merchantProfile = merchantProfileRepository.findByUser(user)
                .orElseThrow(() -> new TapprException("Merchant profile not found"));
        
        // Remove merchant profile
        merchantProfileRepository.delete(merchantProfile);
        
        // Downgrade user role to REGULAR
        user.setRole(Role.REGULAR);
        user.setMerchantProfile(null);
        userRepository.save(user);
        
        log.info("Merchant profile deactivated successfully for user: {}", user.getEmail());
    }
    
    private MerchantProfileResponse mapToResponse(MerchantProfile merchantProfile) {
        MerchantProfileResponse response = new MerchantProfileResponse();
        response.setId(merchantProfile.getId());
        response.setBusinessName(merchantProfile.getBusinessName());
        response.setBusinessType(merchantProfile.getBusinessType());
        response.setBankAccountNumber(merchantProfile.getBankAccountNumber());
        response.setBankName(merchantProfile.getBankName());
        response.setDeviceId(merchantProfile.getDeviceId());
        response.setUserId(merchantProfile.getUser().getId());
        response.setUserEmail(merchantProfile.getUser().getEmail());
        response.setUserFullName(merchantProfile.getUser().getFullName());
        return response;
    }
}