package com.semicolon.africa.tapprbackend.transaction.services.interfaces;

import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<RegisterUserResponse> registerAdmin(StandardRegisterRequest request);
    Mono<RegisterUserResponse> registerWithStandardLogin(StandardRegisterRequest request);
    Mono<RegisterUserResponse> registerWithZkLogin(ZkRegisterRequest request);
    Mono<LoginResponse> loginWithStandardLogin(StandardLoginRequest request);
    Mono<LoginResponse> loginWithZkLogin(ZkLoginRequest request);
    Mono<LogoutUserResponse> logoutUser(LogoutRequest request);
    Mono<String> generateAccessToken(String username);
    boolean validateAccessToken(String token);
    String generateRefreshToken(String userId);
    boolean validateRefreshToken(String token);
    Mono<TokenPair> refreshTokens(String refreshToken);
}
