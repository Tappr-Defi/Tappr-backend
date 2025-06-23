package com.semicolon.africa.tapprbackend.user.dtos.requests;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {
    @JsonAlias({"username", "userName"})
    private String username;
    private String password;
    private String loginMethod;
    private String zkProof;
    private String publicKey;
}