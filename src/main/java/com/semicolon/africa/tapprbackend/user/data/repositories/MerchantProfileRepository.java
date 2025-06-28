package com.semicolon.africa.tapprbackend.user.data.repositories;

import com.semicolon.africa.tapprbackend.user.data.models.MerchantProfile;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, UUID> {
    Optional<MerchantProfile> findByUser(User user);
    Optional<MerchantProfile> findByUserId(UUID userId);
    boolean existsByUser(User user);
    boolean existsByUserId(UUID userId);
}