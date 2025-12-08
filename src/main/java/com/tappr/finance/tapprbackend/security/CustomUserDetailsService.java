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
        String trimmedLoginId = loginId.trim();

        Optional<User> optionalUser;

        optionalUser = userRepository.findByEmail(trimmedLoginId);

        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByPhoneNumber(trimmedLoginId);
        }

        User user = optionalUser.orElseThrow(
                () -> new UsernameNotFoundException(
                        "User not found with identifier: " + loginId
                )
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}