//package com.semicolon.africa.tapprbackend.user.controllers;
//
//import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
//import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
//import com.semicolon.africa.tapprbackend.user.dtos.requests.LogoutRequest;
//import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
//import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
//import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
//import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
//import com.semicolon.africa.tapprbackend.tapprException.TapprException;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin(origins = "*")
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("/register")
//    public ResponseEntity<CreateNewUserResponse> register(@Valid @RequestBody CreateNewUserRequest request) {
//
//            log.info("Registering new user with email: {}", request.getEmail());
//            CreateNewUserResponse response = authService.createNewUser(request);
//            log.info("User registered successfully with ID: {}", response.getUserId());
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//
////        try {
////            log.info("Registering new user with email: {}", request.getEmail());
////            CreateNewUserResponse response = authService.createNewUser(request);
////            log.info("User registered successfully with ID: {}", response.getUserId());
////            return ResponseEntity.status(HttpStatus.CREATED).body(response);
////        } catch (TapprException e) {
////            log.error("Registration failed for email {}: {}", request.getEmail(), e.getMessage());
////            throw e;
////        } catch (Exception e) {
////            log.error("Unexpected error during registration for email {}: {}", request.getEmail(), e.getMessage());
////            throw new TapprException("Registration failed due to an unexpected error");
////        }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
//        try {
//            log.info("Login attempt for email: {}", request.getEmail());
//            LoginResponse response = authService.login(request);
//            log.info("User logged in successfully: {}", request.getEmail());
//            return ResponseEntity.ok(response);
//        } catch (TapprException e) {
//            log.error("Login failed for email {}: {}", request.getEmail(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error during login for email {}: {}", request.getEmail(), e.getMessage());
//            throw new TapprException("Login failed due to an unexpected error");
//        }
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<LogoutUserResponse> logout(@Valid @RequestBody LogoutRequest request) {
//        try {
//            log.info("Logout attempt for email: {}", request.getEmail());
//            LogoutUserResponse response = authService.logOut(request);
//            log.info("User logged out successfully: {}", request.getEmail());
//            return ResponseEntity.ok(response);
//        } catch (TapprException e) {
//            log.error("Logout failed for email {}: {}", request.getEmail(), e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            log.error("Unexpected error during logout for email {}: {}", request.getEmail(), e.getMessage());
//            throw new TapprException("Logout failed due to an unexpected error");
//        }
//    }
//
//    @GetMapping("/health")
//    public ResponseEntity<String> health() {
//        return ResponseEntity.ok("Auth service is running");
//    }
//}
package com.semicolon.africa.tapprbackend.user.controllers;

import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LogoutRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<CreateNewUserResponse> registerUser(@Valid @RequestBody CreateNewUserRequest request) {
        CreateNewUserResponse response = authService.createNewUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutUserResponse> logout(@Valid @RequestBody LogoutRequest request) {
        LogoutUserResponse response = authService.logOut(request);
        return ResponseEntity.ok(response);
    }
}
