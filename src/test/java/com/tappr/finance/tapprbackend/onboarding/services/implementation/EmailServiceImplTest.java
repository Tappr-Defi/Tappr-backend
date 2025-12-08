package com.tappr.finance.tapprbackend.onboarding.services.implementation;

import com.tappr.finance.tapprbackend.onboarding.services.interfaces.VerificationTokenService;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import com.tappr.finance.tapprbackend.user.exceptions.EmailSendException;
import com.tappr.finance.tapprbackend.user.services.implementations.WebhookService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private WebhookService webhookService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenService verificationTokenService;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }


    @Test
    void sendVerificationEmail_ShouldSuccess() {
        emailService.sendVerificationEmail("test@example.com", "TestUser", "123456");

        verify(mailSender, times(1)).send(mimeMessage);
        verify(webhookService, times(1)).emitWebhook(eq("email.sent"), eq("test@example.com"), anyString());
    }

    @Test
    void sendVerificationEmail_ShouldThrowEmailSendException_WhenMailSenderFails() {
        doThrow(new org.springframework.mail.MailSendException("Connection refused"))
                .when(mailSender).send(any(MimeMessage.class));

        EmailSendException exception = assertThrows(EmailSendException.class, () ->
                emailService.sendVerificationEmail("test@example.com", "TestUser", "123456")
        );

        assertTrue(exception.getMessage().contains("verification_otp"));
        verify(webhookService, never()).emitWebhook(any(), any(), any());
    }

    @Test
    void sendVerificationEmail_ShouldNotFail_WhenWebhookFails() {
        doThrow(new RuntimeException("Webhook error"))
                .when(webhookService).emitWebhook(any(), any(), any());

        assertDoesNotThrow(() ->
                emailService.sendVerificationEmail("test@example.com", "TestUser", "123456")
        );

        verify(mailSender, times(1)).send(mimeMessage); // Email still sent
    }

    @Test
    void resendVerificationEmail_ShouldSuccess_WhenUserExistsAndUnverified() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("TestUser");
        user.setVerified(false);

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(verificationTokenService.generateToken(user)).thenReturn("654321");

        emailService.resendVerificationEmail("test@example.com");

        verify(mailSender, times(1)).send(mimeMessage);
        verify(verificationTokenService).generateToken(user);
    }

    @Test
    void resendVerificationEmail_ShouldDoNothing_WhenUserAlreadyVerified() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setVerified(true);

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));

        emailService.resendVerificationEmail("test@example.com");

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(verificationTokenService, never()).generateToken(any());
    }

    @Test
    void resendVerificationEmail_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                emailService.resendVerificationEmail("unknown@example.com")
        );

        assertEquals("User not found", ex.getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }


    @Test
    void sendEmailVerifiedEmail_ShouldSuccess() {
        emailService.sendEmailVerifiedEmail("test@example.com", "Test", "http://dash");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_ShouldSuccess() {
        emailService.sendPasswordResetEmail("test@example.com", "Test", "http://reset");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendNewLoginAlertEmail_ShouldSuccess() {
        emailService.sendNewLoginAlertEmail("test@example.com", "Test", "http://secure");
        verify(mailSender).send(mimeMessage);
    }
}