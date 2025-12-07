package com.tappr.finance.tapprbackend.user.data.repositories;

import com.tappr.finance.tapprbackend.user.data.models.User;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(@NotEmpty(message = "phone number cannot be empty") String phoneNumber);

    boolean existsByEmailIgnoreCase(@NotEmpty(message = "email address cannot be empty") String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByPhoneNumber(@NotEmpty(message = "phone number cannot be empty") String phoneNumber);

    Optional<User> findById(UUID userId);

    Optional<User> findUserByUsername(String username);
}