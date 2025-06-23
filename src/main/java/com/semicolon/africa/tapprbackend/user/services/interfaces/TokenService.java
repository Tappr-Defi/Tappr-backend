package com.semicolon.africa.tapprbackend.user.services.interfaces;

import org.vomzersocials.zkLogin.models.Token;
import reactor.core.publisher.Mono;

public interface TokenService {
    Mono<String> createToken(String userName);
    Mono<Token> findByUserName(String userName);
    Mono<Void> deleteToken(String id);
}
