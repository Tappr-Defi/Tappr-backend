package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterUserRequest {
    @JsonAlias({"username", "userName"})
    private String userName;
    private String password;
    private String zkProof;
    private String publicKey;
}
