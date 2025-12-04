package com.tappr.finance.tapprbackend.security;

import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // Trim the input for cleaner processing
        String trimmedLoginId = loginId.trim();

        Optional<User> optionalUser;

        // 1. Attempt to find the user by the input as an EMAIL
        optionalUser = userRepository.findByEmail(trimmedLoginId);

        // 2. If not found by email, attempt to find the user by PHONE NUMBER
        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByPhoneNumber(trimmedLoginId);
        }

        // 3. If still not found, throw the exception
        User user = optionalUser.orElseThrow(
                () -> new UsernameNotFoundException(
                        "User not found with identifier: " + loginId
                )
        );

        // 4. Build and return the Spring Security UserDetails object
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail()) // Typically use the email as the unique Spring Security username principal
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}