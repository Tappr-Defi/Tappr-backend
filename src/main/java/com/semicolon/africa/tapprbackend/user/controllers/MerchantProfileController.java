package com.semicolon.africa.tapprbackend.user.controllers;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.tapprException.TapprException;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateMerchantProfileRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.MerchantProfileResponse;
import com.semicolon.africa.tapprbackend.user.services.interfaces.MerchantProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MerchantProfileController {
    
    private final MerchantProfileService merchantProfileService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/activate")
    public ResponseEntity<Map<String, Object>> activateMerchantProfile(
            @Valid @RequestBody CreateMerchantProfileRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            
            MerchantProfileResponse merchantProfile = merchantProfileService.createMerchantProfile(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Merchant profile activated successfully");
            response.put("merchantProfile", merchantProfile);
            
            log.info("Merchant profile activated for user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (TapprException e) {
            log.error("Error activating merchant profile: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error activating merchant profile: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to activate merchant profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getMerchantProfile(HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            
            MerchantProfileResponse merchantProfile = merchantProfileService.getMerchantProfile(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("merchantProfile", merchantProfile);
            
            return ResponseEntity.ok(response);
            
        } catch (TapprException e) {
            log.error("Error getting merchant profile: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error getting merchant profile: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to retrieve merchant profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateMerchantProfile(
            @Valid @RequestBody CreateMerchantProfileRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            
            MerchantProfileResponse merchantProfile = merchantProfileService.updateMerchantProfile(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Merchant profile updated successfully");
            response.put("merchantProfile", merchantProfile);
            
            return ResponseEntity.ok(response);
            
        } catch (TapprException e) {
            log.error("Error updating merchant profile: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error updating merchant profile: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update merchant profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateMerchantProfile(HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            
            merchantProfileService.deactivateMerchantProfile(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Merchant profile deactivated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (TapprException e) {
            log.error("Error deactivating merchant profile: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Unexpected error deactivating merchant profile: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to deactivate merchant profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMerchantStatus(HttpServletRequest httpRequest) {
        try {
            UUID userId = getUserIdFromToken(httpRequest);
            
            boolean hasMerchantProfile = merchantProfileService.hasMerchantProfile(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isMerchant", hasMerchantProfile);
            response.put("message", hasMerchantProfile ? "User has merchant profile" : "User does not have merchant profile");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Unexpected error checking merchant status: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to check merchant status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    private UUID getUserIdFromToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            throw new TapprException("Authorization token is required");
        }
        
        if (!jwtUtil.validateToken(token)) {
            throw new TapprException("Invalid or expired token");
        }
        
        String userIdString = jwtUtil.extractUserId(token);
        return UUID.fromString(userIdString);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}