//package com.semicolon.africa.tapprbackend.user.controllers;
//
//import com.semicolon.africa.tapprbackend.security.JwtUtil;
//import com.semicolon.africa.tapprbackend.user.data.models.User;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;
//
//    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
//            throw new RuntimeException("Invalid credentials");
//        }
//
//        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
//        return ResponseEntity.ok(new AuthResponse(token));
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
//        if (userRepository.existsByEmail(request.getEmail())) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
//        }
//
//        User user = new User();
//        user.setEmail(request.getEmail());
//        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//        user.setRole(Role.MERCHANT);
//        userRepository.save(user);
//
//        return ResponseEntity.ok("User registered");
//    }
//}
//
