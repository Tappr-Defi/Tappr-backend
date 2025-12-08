package com.tappr.finance.tapprbackend.onboarding.services.implementation;

import com.tappr.finance.tapprbackend.general.template.EmailTemplate;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.VerificationTokenService;
import com.tappr.finance.tapprbackend.user.data.models.User;
import com.tappr.finance.tapprbackend.user.data.repositories.UserRepository;
import com.tappr.finance.tapprbackend.user.exceptions.EmailSendException;
import com.tappr.finance.tapprbackend.user.services.implementations.WebhookService;
import com.tappr.finance.tapprbackend.onboarding.services.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("📧 Sending verification email to {} with OTP: {}", to, otp);
        sendEmail(to, "Your Tappr Verification Code",
                EmailTemplate.verifyEmailHtml(firstName, otp),
                EmailTemplate.verifyEmailText(firstName, otp),
                "verification_otp");
    }

    @Async
    @Override
    public void resendVerificationEmail(@NonNull String email) {
        log.info("🔄 Resending verification email to: {}", email);

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();

        if (user.isVerified()) {
            log.info("User already verified: {}", email);
            return;
        }

        String otp = verificationTokenService.generateToken(user);

        sendEmail(user.getEmail(), "Your New Tappr Verification Code",
                EmailTemplate.verifyEmailHtml(user.getFirstName(), otp),
                EmailTemplate.verifyEmailText(user.getFirstName(), otp),
                "verification_otp_resent");
    }

    @Async
    @Override
    public void sendEmailVerifiedEmail(@NonNull String to, @NonNull String firstName, @NonNull String dashboardUrl) {
        sendEmail(
                to,
                "Your Tappr Account is Verified",
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
                "Reset Your Tappr Password",
                EmailTemplate.passwordResetHtml(firstName, resetUrl),
                EmailTemplate.passwordResetText(firstName, resetUrl),
                "password_reset"
        );
    }
    @Async
    @Override
    public void sendNewLoginAlertEmail(@NonNull String email, @NonNull String firstName, @NonNull String secureAccountUrl) {
        sendEmail(
                email,
                "New Sign-in to Your Tappr Account",
                EmailTemplate.newLoginHtml(firstName, secureAccountUrl),
                EmailTemplate.newLoginText(firstName, secureAccountUrl),
                "new_login"
        );
    }

}
