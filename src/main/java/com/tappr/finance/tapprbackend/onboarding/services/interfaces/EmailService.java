package com.tappr.finance.tapprbackend.onboarding.services.interfaces;

public interface EmailService {
    void sendVerificationEmail(String to, String firstName, String otp);
    void resendVerificationEmail(String email);  // Ensure this exists
    void sendEmailVerifiedEmail(String to, String firstName, String dashboardUrl);
    void sendPasswordResetEmail(String to, String firstName, String resetUrl);
    void sendNewLoginAlertEmail(String to, String firstName, String secureAccountUrl);
}
