package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.enums.LoginMethod;
import org.vomzersocials.user.enums.Role;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.user.springSecurity.JwtUtil;
import org.vomzersocials.zkLogin.services.ZkLoginService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

import static org.vomzersocials.user.utils.ValidationUtils.isValidPassword;
import static org.vomzersocials.user.utils.ValidationUtils.isValidUsername;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ZkLoginService zkLoginService;
    private final WalletApiClient walletApiClient;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final WebClient webClient;


    @Value("${app.node-service.generate-wallet-url}")
    private String generateWalletUrl;

    @Value("${app.node-service.verify-login-url}")
    private String verifyLoginUrl;

    public AuthenticationServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                                     ZkLoginService zkLoginService, WalletApiClient walletApiClient, JwtUtil jwtUtil, WebClient.Builder webClientBuilder,
                                     RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.zkLoginService = zkLoginService;
        this.walletApiClient = walletApiClient;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<RegisterUserResponse> registerWithStandardLogin(StandardRegisterRequest request) {
        log.info("Attempting standard registration for user: {}", request.getUserName());
        validateUserInput(request.getUserName(), request.getPassword());
        String userId = UUID.randomUUID().toString();
        log.debug("Generated userId: {}", userId);

        return findExistingUser(request.getUserName())
                .flatMap(existingUser -> {
                    if (existingUser.isPresent()) {
                        log.error("Username already exists: {}", request.getUserName());
                        return Mono.error(new IllegalArgumentException("Username already exists"));
                    }
                    return findExistingUserById(userId)
                            .flatMap(existingUserById -> {
                                if (existingUserById.isPresent()) {
                                    log.error("Generated userId already exists: {}", userId);
                                    return Mono.error(new IllegalArgumentException("Generated userId already exists"));
                                }
                                String jwt = jwtUtil.generateAccessToken(userId, List.of(Role.USER.name()));
                                Map<String, String> requestBody = Map.of("username", request.getUserName(), "jwt", jwt);
                                return webClient.post()
                                        .uri(generateWalletUrl)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(requestBody)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                        .flatMap(walletDetails -> {
                                            if (walletDetails == null || !Boolean.TRUE.equals(walletDetails.get("success"))) {
                                                String error = walletDetails != null ? (String) walletDetails.get("error") : "Invalid response";
                                                return Mono.error(new IllegalArgumentException("Node.js error: " + error));
                                            }
                                            String walletAddress = (String) walletDetails.get("walletAddress");
                                            String publicKey = (String) walletDetails.get("publicKey");
                                            String salt = (String) walletDetails.get("salt");
                                            if (walletAddress == null || publicKey == null || salt == null) {
                                                return Mono.error(new IllegalArgumentException("Missing wallet details"));
                                            }
                                            return createUser(
                                                    userId,
                                                    request.getUserName(),
                                                    request.getPassword(),
                                                    walletAddress,
                                                    salt,
                                                    Role.USER
                                            ).map(user -> registerNewUserResponse(request.getUserName(), user));
                                        });
                            });
                })
                .onErrorMap(e -> {
                    log.error("Registration failed for user {}: {}", request.getUserName(), e.getMessage(), e);
                    return new IllegalArgumentException("Registration failed: " + e.getMessage(), e);
                });
    }

    @Override
    public Mono<RegisterUserResponse> registerWithZkLogin(ZkRegisterRequest request) {
        log.info("Attempting zkLogin registration for user: {}", request.getUserName());
        if (!isValidUsername(request.getUserName())) {
            return Mono.error(new IllegalArgumentException("Invalid username"));
        }
        if (!jwtUtil.validateToken(request.getJwt())) {
            log.error("Invalid JWT for user: {}", request.getUserName());
            return Mono.error(new IllegalArgumentException("Invalid or expired JWT"));
        }
        String userId;
        try {
            userId = jwtUtil.extractUsername(request.getJwt());
            log.debug("Extracted userId from JWT: {}", userId);
        } catch (Exception e) {
            log.error("Failed to parse JWT for user {}: {}", request.getUserName(), e.getMessage(), e);
            return Mono.error(new IllegalArgumentException("Invalid JWT format"));
        }
        if (userId == null || userId.isEmpty()) {
            log.error("No user identifier in JWT for user: {}", request.getUserName());
            return Mono.error(new IllegalArgumentException("No user identifier in JWT"));
        }

        return findExistingUserById(userId)
                .flatMap(existingUser -> {
                    if (existingUser.isPresent()) {
                        log.error("User already exists for userId: {}", userId);
                        return Mono.error(new IllegalArgumentException("User already exists"));
                    }
                    Map<String, String> requestBody = Map.of(
                            "username", request.getUserName(),
                            "jwt", request.getJwt()
                    );
                    log.info("Sending request to Node.js service at {}: {}", generateWalletUrl, requestBody);
                    return webClient.post()
                            .uri(generateWalletUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .doOnError(error -> log.error("WebClient error for {}: {}", generateWalletUrl, error.getMessage(), error))
                            .flatMap(walletDetails -> {
                                log.info("Node.js service response for userId {}: Status=200, Body={}", userId, walletDetails);

                                if (walletDetails == null || !Boolean.TRUE.equals(walletDetails.get("success"))) {
                                    String error = walletDetails != null ? (String) walletDetails.get("error") : "Invalid response";
                                    log.error("Node.js service error for userId {}: {}", userId, error);
                                    return Mono.error(new IllegalArgumentException("Node.js error: " + error));
                                }
                                String walletAddress = (String) walletDetails.get("walletAddress");
                                String publicKey = (String) walletDetails.get("publicKey");
                                String salt = (String) walletDetails.get("salt");
                                if (walletAddress == null || publicKey == null || salt == null) {
                                    log.error("Missing wallet details for userId {}: walletAddress={}, publicKey={}, salt={}",
                                            userId, walletAddress, publicKey, salt);
                                    return Mono.error(new IllegalArgumentException("Missing wallet details"));
                                }
                                return createUser(
                                        userId,
                                        request.getUserName(),
                                        null,
                                        walletAddress,
                                        salt,
                                        Role.USER
                                ).flatMap(user -> {
                                    user.setPublicKey(publicKey);
                                    return Mono.fromCallable(() -> userRepository.save(user))
                                            .subscribeOn(Schedulers.boundedElastic());
                                }).map(user -> new RegisterUserResponse(
                                        true,
                                        "Registration successful",
                                        user.getSuiAddress(),
                                        user.getUserName(),
                                        200,
                                        walletDetails
                                ));
                            })
                            .onErrorMap(e -> {
                                log.error("Failed to communicate with Node.js service for userId {}: {}", userId, e.getMessage(), e);
                                return new IllegalArgumentException("Failed to communicate with Node.js service: " + e.getMessage(), e);
                            });
                })
                .onErrorMap(e -> {
                    log.error("zkRegistration failed for user {}: {}", request.getUserName(), e.getMessage(), e);
                    return new IllegalArgumentException("zkRegistration failed: " + e.getMessage(), e);
                });
    }

    @Override
    public Mono<RegisterUserResponse> registerAdmin(StandardRegisterRequest request) {
        log.info("Attempting admin registration for user: {}", request.getUserName());
        validateUserInput(request.getUserName(), request.getPassword());
        String salt = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        return findExistingUser(request.getUserName())
                .flatMap(existingUser -> {
                    if (existingUser.isPresent()) {
                        return Mono.error(new IllegalArgumentException("Username already exists"));
                    }
                    return walletApiClient.generateSuiAddress()
                            .flatMap(address -> createUser(
                                    userId,
                                    request.getUserName(),
                                    request.getPassword(),
                                    address,
                                    salt,
                                    Role.ZKSocials
                            ).map(user -> registerNewUserResponse(request.getUserName(), user)));
                })
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Admin registration failed: " + e.getMessage()));
    }

    @Override
    public Mono<LoginResponse> loginWithStandardLogin(StandardLoginRequest request) {
        log.info("Attempting standard login for user: {}", request.getUserName());
        request.setLoginMethod(LoginMethod.STANDARD_LOGIN);
        return handleStandardLogin(request)
                .flatMap(user -> Mono.fromCallable(() -> {
                            user.setIsLoggedIn(true);
                            return userRepository.save(user);
                        }).subscribeOn(Schedulers.boundedElastic())
                        .thenReturn(user))

                .map(user -> createLoginResponse(user, request.getLoginMethod().name()))
                .doOnNext(this::logLoginResponse)
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Login failed: " + e.getMessage()));
    }

    @Override
    public Mono<LoginResponse> loginWithZkLogin(ZkLoginRequest request) {
        log.info("Attempting zkLogin with JWT: {}", request.getJwt());

        if (!jwtUtil.validateToken(request.getJwt())) {
            log.error("Invalid or expired JWT");
            return Mono.error(new IllegalArgumentException("Invalid or expired JWT"));
        }
        String userId;
        try {
            userId = jwtUtil.extractUsername(request.getJwt());
            log.debug("Extracted userId from JWT: {}", userId);
        } catch (Exception e) {
            log.error("Failed to parse JWT: {}", e.getMessage(), e);
            return Mono.error(new IllegalArgumentException("Invalid JWT format"));
        }
        if (userId == null || userId.isEmpty()) {
            log.error("No user identifier found in JWT");
            return Mono.error(new IllegalArgumentException("No user identifier in JWT"));
        }
        return findExistingUserById(userId)
                .flatMap(existingUser -> {
                    if (existingUser.isEmpty()) {
                        log.error("User not found for userId: {}", userId);
                        return Mono.error(new IllegalArgumentException("User not found"));
                    }
                    User user = existingUser.get();
                    Map<String, String> requestBody = Map.of("jwt", request.getJwt());
                    log.info("Sending request to Node.js service at {}: {}", verifyLoginUrl, requestBody);

                    return webClient.post()
                            .uri(verifyLoginUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(HashMap.class)
                            .doOnError(error -> log.error("WebClient error for {}: {}", verifyLoginUrl, error.getMessage(), error))
                            .flatMap(walletDetails -> {
                                log.info("Node.js service response for userId {}: {}", userId, walletDetails);

                                if (walletDetails == null) {
                                    log.error("Node.js service returned null response for userId: {}", userId);
                                    return Mono.error(new IllegalArgumentException("Node.js service returned null response"));
                                }
                                if (!Boolean.TRUE.equals(walletDetails.get("success"))) {
                                    String error = walletDetails.get("error") != null ? (String) walletDetails.get("error") : "Invalid response";
                                    log.error("Node.js service error for userId {}: {}", userId, error);
                                    return Mono.error(new IllegalArgumentException("Node.js error: " + error));
                                }
                                String walletAddress = (String) walletDetails.get("walletAddress");
                                String publicKey = (String) walletDetails.get("publicKey");
                                if (walletAddress == null || publicKey == null) {
                                    log.error("Missing wallet details for userId {}: walletAddress={}, publicKey={}",
                                            userId, walletAddress, publicKey);
                                    return Mono.error(new IllegalArgumentException("Missing wallet details: address=" + walletAddress + ", publicKey=" + publicKey));
                                }
                                log.info("Comparing stored [suiAddress={}, publicKey={}] with Node.js response [walletAddress={}, publicKey={}]",
                                        user.getSuiAddress(), user.getPublicKey(), walletAddress, publicKey);
                                if (!user.getSuiAddress().equals(walletAddress) || !user.getPublicKey().equals(publicKey)) {
                                    log.error("Wallet details mismatch for userId {}: stored [suiAddress={}, publicKey={}], received [walletAddress={}, publicKey={}]",
                                            userId, user.getSuiAddress(), user.getPublicKey(), walletAddress, publicKey);
                                    return Mono.error(new IllegalArgumentException("Wallet details mismatch"));
                                }
                                user.setIsLoggedIn(true);
                                return Mono.fromCallable(() -> {
                                    log.info("Updating isLoggedIn=true for userId: {}", userId);
                                    try {
                                        User updatedUser = userRepository.save(user);
                                        log.info("Updated user: {}", updatedUser);
                                        return updatedUser;
                                    } catch (Exception e) {
                                        log.error("Failed to save userId {}: {}", userId, e.getMessage(), e);
                                        throw new IllegalArgumentException("Database error: " + e.getMessage(), e);
                                    }
                                }).map(updatedUser -> createLoginResponse(updatedUser, "ZK_LOGIN"));
                            })
                            .onErrorMap(e -> {
                                log.error("Unexpected error for userId {}: {}", userId, e.getMessage(), e);
                                return new IllegalArgumentException("Failed to communicate with Node.js service: " + e.getMessage(), e);
                            });
                })
                .onErrorMap(e -> {
                    log.error("zkLogin failed for userId {}: {}", userId, e.getMessage(), e);
                    return new IllegalArgumentException("zkLogin failed: " + e.getMessage(), e);
                });
    }

    private Mono<Optional<User>> findExistingUserById(String userId) {
        return Mono.fromCallable(() -> {
            log.debug("Searching for user with userId: {}", userId);
            try {
                Optional<User> user = userRepository.findById(userId);
                log.debug("Found user for userId {}: {}", userId, user.isPresent() ? user.get() : "not found");
                return user;
            } catch (Exception e) {
                log.error("Error finding user with userId {}: {}", userId, e.getMessage(), e);
                throw new IllegalArgumentException("Database error while finding user: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Optional<User>> findUserByPublicKey(@NotEmpty(message = "Public key cannot be empty") String publicKey) {
        return Mono.fromCallable(() -> userRepository.findByPublicKey(publicKey))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<LogoutUserResponse> logoutUser(LogoutRequest request) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findUserByUserName(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setIsLoggedIn(false);
            userRepository.save(user);
            return new LogoutUserResponse(user.getUserName(), "Logged out successfully");
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> generateAccessToken(String username) {
        return Mono.fromCallable(() -> userRepository.findUserByUserName(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .map(user -> jwtUtil.generateAccessToken(username, List.of(user.getRole().name())));
    }

    @Override
    public boolean validateAccessToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public String generateRefreshToken(String userId) {
        return jwtUtil.generateRefreshToken(userId);
    }

    @Override
    public boolean validateRefreshToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public Mono<TokenPair> refreshTokens(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        return Mono.fromCallable(() -> {
            User user = userRepository.findUserByUserName(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String newAccessToken = jwtUtil.generateAccessToken(username, List.of(user.getRole().name()));
            String newRefreshToken = jwtUtil.generateRefreshToken(username);
            return new TokenPair(newAccessToken, newRefreshToken, "Successful");
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void validateUserInput(String username, String password) {
        boolean isZkLogin = password == null;
        if (!isValidUsername(username) || (!isZkLogin && !isValidPassword(password))) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    private Mono<Optional<User>> findExistingUser(String userName) {
        return Mono.fromCallable(() -> userRepository.findUserByUserName(userName))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<User> handleStandardLogin(StandardLoginRequest req) {
        return Mono.fromCallable(() -> {
            log.info("Standard login with username='{}'", req.getUserName());
            User user = userRepository.findUserByUserName(req.getUserName())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
            boolean matches = passwordEncoder.matches(req.getPassword(), user.getPassword());
            if (!matches) throw new IllegalArgumentException("Invalid username or password");
            return user;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private LoginResponse createLoginResponse(User user, String loginMethod) {
        String accessToken = jwtUtil.generateAccessToken(user.getUserName(), List.of(user.getRole().name()));
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserName());
        return new LoginResponse(
                user.getUserName(),
                "Logged in successfully",
                accessToken,
                refreshToken,
                user.getRole(),
                loginMethod
        );
    }

    private void logLoginResponse(LoginResponse resp) {
        try {
            String json = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(resp);
            log.info("LoginResponse JSON:\n{}", json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize LoginResponse", e);
        }
    }

    private Mono<User> createUser(String userId, String username, String password, String suiAddress, String salt, Role role) {
        return Mono.fromCallable(() -> {
            User user = new User();
            user.setId(userId);
            user.setUserName(username);
            if (password != null) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setRole(role != null ? role : Role.USER);
            user.setSuiAddress(suiAddress);
            user.setPublicKey(null);
            user.setSalt(salt);
            user.setIsLoggedIn(false);
            log.info("Saving user: userId={}, username={}, suiAddress={}, salt={}",
                    userId, username, suiAddress, salt);
            try {
                User savedUser = userRepository.save(user);
                log.info("Saved user: {}", savedUser);
                return savedUser;
            } catch (Exception e) {
                log.error("Failed to save user: {}", e.getMessage(), e);
                throw new IllegalArgumentException("Database error: " + e.getMessage(), e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private RegisterUserResponse registerNewUserResponse(String username, User user) {
        RegisterUserResponse response = new RegisterUserResponse();
        response.setUsername(user.getUserName());
        response.setSuccess(true);
        response.setMessage("User registered successfully.");
        response.setSuiAddress(user.getSuiAddress());
        return response;
    }

    private Mono<String> verifyZkProofAndRegisterOrThrow(String userName, String jwt) {
        log.info("Verifying username: {}, jwt: {}", userName, jwt);
        String suiAddress = String.valueOf(zkLoginService.registerViaZkLogin(userName, jwt));
//        boolean isValidZkProof = Boolean.TRUE.equals(zkLoginService(userName, jwt));
//        if (!isValidZkProof) {
//            return Mono.error(new IllegalArgumentException("Invalid zkProof"));
//        }
        return Mono.just(suiAddress);
    }
}