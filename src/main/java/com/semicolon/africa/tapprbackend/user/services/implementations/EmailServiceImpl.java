package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.general.template.EmailTemplate;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.exceptions.EmailSendException;
import com.semicolon.africa.tapprbackend.user.services.interfaces.EmailService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.VerificationTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final WebhookService webhookService;
    private final UserRepository userRepository;
    private final VerificationTokenService verificationTokenService;

    private void sendEmail(
            @NonNull String to,
            @NonNull String subject,
            @NonNull String htmlBody,
            @NonNull String textBody,
            @NonNull String eventType
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);

            mailSender.send(mimeMessage);

            try {
                webhookService.emitWebhook("email.sent", to, subject);
            } catch (Exception webhookEx) {
                log.warn("Webhook failed after sending {} email to {}: {}", eventType, to, webhookEx.getMessage());
            }

            log.info("Sent {} email to {}", eventType, to);

        } catch (MessagingException | org.springframework.mail.MailException e) {
            log.error("Failed to send {} email to {}: {}", eventType, to, e.getMessage());
            throw new EmailSendException("Email sending failed for " + eventType, e);
        }
    }

    @Async
    @Override
    public void sendVerificationEmail(@NonNull String to, @NonNull String firstName, @NonNull String otp) {
        sendEmail(
                to,
                "Your QueueNI Verification Code",
                EmailTemplate.verifyEmailHtml(firstName, otp),
                EmailTemplate.verifyEmailText(firstName, otp),
                "verification_otp"
        );
    }

    @Async
    public void resendVerificationEmail(@NonNull String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            log.warn("Resend verification requested for non-existent email: {}", email);
            throw new IllegalArgumentException("No user found with the provided email address.");
        }

        User user = userOpt.get();

        if (user.isVerified()) {
            log.info("User {} already verified. No new OTP sent.", email);
            return;
        }

        String otp = verificationTokenService.generateToken(user);

        sendEmail(
                user.getEmail(),
                "Your New QueueNI Verification Code",
                EmailTemplate.verifyEmailHtml(user.getFirstName(), otp),
                EmailTemplate.verifyEmailText(user.getFirstName(), otp),
                "verification_otp_resent"
        );

        log.info("Resent verification OTP to {}", user.getEmail());
    }

    @Async
    @Override
    public void sendEmailVerifiedEmail(@NonNull String to, @NonNull String firstName, @NonNull String dashboardUrl) {
        sendEmail(
                to,
                "Your QueueNI Account is Verified",
                EmailTemplate.emailVerifiedHtml(firstName, dashboardUrl),
                EmailTemplate.emailVerifiedText(firstName, dashboardUrl),
                "email_verified"
        );
    }

    @Async
    @Override
    public void sendPasswordResetEmail(@NonNull String to, @NonNull String firstName, @NonNull String resetUrl) {
        sendEmail(
                to,
                "Reset Your QueueNI Password",
                EmailTemplate.passwordResetHtml(firstName, resetUrl),
                EmailTemplate.passwordResetText(firstName, resetUrl),
                "password_reset"
        );
    }

    @Async
    @Override
    public void sendNewLoginAlertEmail(@NonNull String to, @NonNull String firstName, @NonNull String secureAccountUrl) {
        sendEmail(
                to,
                "New Sign-in to Your QueueNI Account",
                EmailTemplate.newLoginHtml(firstName, secureAccountUrl),
                EmailTemplate.newLoginText(firstName, secureAccountUrl),
                "new_login"
        );
    }

}
