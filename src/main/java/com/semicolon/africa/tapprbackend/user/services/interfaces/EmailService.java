package com.semicolon.africa.tapprbackend.user.services.interfaces;

import lombok.NonNull;

public interface EmailService {
    void sendVerificationEmail(String to, String firstName, String verificationUrl);
    void resendVerificationEmail(@NonNull String email);
    void sendEmailVerifiedEmail(String to, String firstName, String dashboardUrl);
    void sendPasswordResetEmail(String to, String firstName, String resetUrl);
    void sendNewLoginAlertEmail(String to, String firstName, String secureAccountUrl);
}
