package com.semicolon.africa.tapprbackend.user.data.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.semicolon.africa.tapprbackend.reciepts.enums.Role;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    private String email;
    private String phone;
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role; // e.g. MERCHANT

    private boolean isKycVerified;
    private boolean isTier2Verified;

    private LocalDateTime createdAt;


}
