package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.exceptions.WalletCreationFailedException;
import com.semicolon.africa.tapprbackend.Wallet.service.interfaces.WalletService;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.tapprException.TapprException;
import com.semicolon.africa.tapprbackend.user.data.models.RefreshToken;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LogoutRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.exceptions.UserNotFoundException;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final RefreshTokenService refreshTokenService;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           WalletService walletService,
                           WalletRepository walletRepository,
                           RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.refreshTokenService = refreshTokenService;
    }


    @Transactional
    @Override
    public CreateNewUserResponse createNewUser(CreateNewUserRequest request) {
        log.info("Creating a new user: {}", request);
        validateSignUpRequest(request);

        String email = request.getEmail().toLowerCase().trim();
        String phone = normalizePhoneNumber(request.getPhoneNumber());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        if (userRepository.existsByPhoneNumber(phone)) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setPhoneNumber(phone);
        user.setRole(Role.REGULAR);
        user.setCreatedAt(LocalDateTime.now());
        user.setKycVerified(false);

        userRepository.save(user);
        log.info("User saved to database: {}", email);

        return new CreateNewUserResponse(
                "User created successfully",
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }




    private void validateSignUpRequest(CreateNewUserRequest request) {
        if (isNullOrEmpty(request.getFirstName())) throw new IllegalArgumentException("First name is required");
        if (isNullOrEmpty(request.getLastName())) throw new IllegalArgumentException("Last name is required");
        if (isNullOrEmpty(request.getEmail())) throw new IllegalArgumentException("Email is required");
        if (isNullOrEmpty(request.getPassword())) throw new IllegalArgumentException("Password is required");
        if (isNullOrEmpty(request.getPhoneNumber())) throw new IllegalArgumentException("Phone number is required");

        if (!request.getEmail().matches("^[\\w+.'-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword().contains(" ")) {
            throw new IllegalArgumentException("Password must not contain whitespace");
        }
    }

    @Transactional
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with that email doesn't exist"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect credentials");
        }

        user.setLoggedIn(true);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

//        createWalletIfNotExists(user);
        createWalletForUser(user);
        return new LoginResponse(
                jwtUtil.generateToken(user.getEmail(), user.getRole()),
                jwtUtil.generateRefreshToken(user.getEmail(), user.getRole()),
                String.valueOf(user.getId()),
                user.getRole(),
                true,
                "Logged in successfully"
        );
    }

    private void createWalletIfNotExists(User user) {
        if (!walletRepository.existsByUser(user)) {
            try {
                walletService.createWalletIfNotExists(user);
                log.info("Wallet created for user: {}", user.getEmail());
            } catch (WalletCreationFailedException e) {
                log.error("Wallet creation failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    private void createWalletForUser(User user) {
        if (!walletRepository.existsByUser(user)) {
            try {
                walletService.createWalletIfNotExists(user);
                log.info("Wallet created for user: {}", user.getEmail());
            } catch (WalletCreationFailedException e) {
                log.error("Wallet creation failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    @Override
    public LoginResponse refreshAccessToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new TapprException("Invalid or expired refresh token"));

        User user = refreshToken.getUser();
        refreshTokenService.revokeAllUserTokens(user);
        RefreshToken newRefresh = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                jwtUtil.generateToken(user.getEmail(), user.getRole()),
                newRefresh.getToken(),
                String.valueOf(user.getId()),
                user.getRole(),
                true,
                "Token refreshed successfully"
        );
    }

    @Override
    public LogoutUserResponse logOut(LogoutRequest logOutRequest) {
        String email = logOutRequest.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User with that email doesn't exist"));

        if (!user.isLoggedIn()) {
            throw new IllegalArgumentException("User is already logged out");
        }

        user.setLoggedIn(false);
        userRepository.save(user);

        return new LogoutUserResponse("Logged Out Successfully", false);
    }

    private boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    private String normalizePhoneNumber(String phone) {
        try {
            return phoneUtil.format(
                    phoneUtil.parse(phone, "NG"),
                    PhoneNumberUtil.PhoneNumberFormat.E164
            );
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }
}





