package com.tappr.finance.tapprbackend.user.controllers;

import com.tappr.finance.tapprbackend.general.dtos.ApiResponse;
import com.tappr.finance.tapprbackend.user.dtos.requests.ProfileSetupRequest;
import com.tappr.finance.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.tappr.finance.tapprbackend.user.dtos.responses.ProfileSetupResponse;
import com.tappr.finance.tapprbackend.user.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management (Secured)", description = "Profile Setup, Settings, and Logout")
public class UserController {

    private final UserService userService;

    // 🔹 Setup profile (PUT because we are updating the user entity)
    @PutMapping("/profile/setup")
    @Operation(summary = "Complete Profile", description = "Set Name and Username. Required after first login.")
    public ResponseEntity<ApiResponse<ProfileSetupResponse>> setupProfile(@Valid @RequestBody ProfileSetupRequest request) {
        return ResponseEntity.ok(userService.setupProfile(request));
    }

    // 🔹 Logout
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate session.")
    public ResponseEntity<LogoutUserResponse> logout() {
        return ResponseEntity.ok(userService.logout().getData());
    }
}