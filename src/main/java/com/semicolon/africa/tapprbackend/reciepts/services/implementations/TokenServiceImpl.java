package com.semicolon.africa.tapprbackend.reciepts.services.implementations;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.repositories.TokenRepository;
import org.vomzersocials.user.services.interfaces.TokenService;
import org.vomzersocials.zkLogin.models.Token;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;

    @Override
    public Mono<String> createToken(String userName) {
        return Mono.fromCallable(() -> {
            String token = generateToken();
            Token userToken = new Token();
            userToken.setId(UUID.randomUUID().toString());
            userToken.setToken(token);
            userToken.setUserName(userName);
            userToken.setTimeCreated(LocalDateTime.now());
            userToken.setExpiresAt(LocalDateTime.now().plusHours(24));
            return tokenRepository.save(userToken).getToken();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String generateToken() {
            SecureRandom secureRandom = new SecureRandom();
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Override
    public Mono<Token> findByUserName(String userName) {
        return Mono.fromCallable(() -> tokenRepository.findTokenByUserName(userName)
                        .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()))
                        .orElseThrow(() -> new UsernameNotFoundException("Invalid or expired token for user: " + userName)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteToken(String id) {
        return Mono.fromCallable(() -> {
            tokenRepository.deleteById(id);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
