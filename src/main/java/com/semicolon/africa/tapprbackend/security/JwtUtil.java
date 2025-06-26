package com.semicolon.africa.tapprbackend.security;

import com.semicolon.africa.tapprbackend.user.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;



@Component
public class JwtUtil {

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}")String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
        return true;
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    public String generateRefreshToken(String email, Role role) {
//        long refreshExpirationMs = jwtExpirationMs * 12;


        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateRefreshToken(String token) {
        return false;
    }
}
