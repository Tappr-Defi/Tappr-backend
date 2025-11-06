package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.user.services.interfaces.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceTest {
    private final EmailService emailService;

    @PostConstruct
    public void testEmailService() {
        try {
            log.info("Testing email service on application startup");
            emailService.sendVerificationEmail(
                    "johdanike@gmail.com",
                    "TestUser",
                    "http://localhost:1946/api/users/verify-email?token=test-token"
            );
            log.info("Test email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send test email: {}", e.getMessage(), e);
        }
    }
}