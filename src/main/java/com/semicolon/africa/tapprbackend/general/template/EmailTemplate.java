package com.semicolon.africa.tapprbackend.general.template;

public class EmailTemplate {

    private static String wrapHtml(String content) {
        return String.format("""
            <html>
              <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background: #f9f9f9; padding: 20px;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: auto; background: white; border-radius: 8px; overflow: hidden;">
                  <tr style="background: #0D9488; color: white;">
                    <td style="padding: 16px; font-size: 18px; font-weight: bold; text-align: center;">
                      Tappr
                    </td>
                  </tr>
                  <tr>
                    <td style="padding: 24px;">
                      %s
                    </td>
                  </tr>
                  <tr style="background: #f3f4f6; font-size: 12px; color: #555;">
                    <td style="padding: 16px; text-align: center;">
                      © 2025 Tappr Inc. All rights reserved.<br>
                      Need help? <a href="mailto:support@tappr.africa" style="color: #0D9488; text-decoration: none;">Contact Support</a><br>
                      <a href="https://tappr.africa/unsubscribe" style="color: #999; text-decoration: none;">Unsubscribe</a>
                    </td>
                  </tr>
                </table>
              </body>
            </html>
            """, content);
    }

    private static String wrapText(String content) {
        return String.format("""
            Tappr

            %s

            ---
            © 2025 Tappr Inc. All rights reserved.
            Need help? Email support@tappr.africa
            Unsubscribe: https://tappr.africa/unsubscribe
            """, content);
    }

    // 🔹 Email Verification (OTP Style)
    public static String verifyEmailHtml(String firstName, String otpCode) {
        String content = String.format("""
            <p>Hi %s,</p>
            <p>Welcome to <strong>Tappr</strong> — your all-in-one softPOS platform for merchants and mobile payments.</p>
            <p>Use the verification code below to confirm your email address and activate your account:</p>
            <p style="text-align: center; margin: 30px 0;">
              <span style="display: inline-block; background: #0D9488; color: #ffffff; 
                           padding: 14px 32px; font-size: 24px; letter-spacing: 6px; 
                           border-radius: 8px; font-weight: bold;">
                %s
              </span>
            </p>
            <p>This code will expire in <strong>15 minutes</strong>.</p>
            <p>If you did not request this, you can safely ignore this email.</p>
            <p>– The Tappr Team</p>
            """, firstName, otpCode);

        return wrapHtml(content);
    }

    public static String verifyEmailText(String firstName, String otpCode) {
        String content = String.format("""
            Hi %s,

            Welcome to Tappr — your all-in-one softPOS platform for merchants and mobile payments.

            Your verification code is: %s

            This code will expire in 15 minutes.

            If you did not request this, please ignore this message.

            – The Tappr Team
            """, firstName, otpCode);

        return wrapText(content);
    }

    // 🔹 Email Verified
    public static String emailVerifiedHtml(String firstName, String dashboardUrl) {
        String content = String.format("""
            <p>Hi %s,</p>
            <p>Your Tappr account has been successfully verified!</p>
            <p>You can now log in and start accepting payments instantly.</p>
            <p style="margin: 24px 0; text-align: center;">
              <a href="%s" style="background: #0D9488; color: white; padding: 12px 20px; text-decoration: none; border-radius: 6px;">
                Go to Dashboard
              </a>
            </p>
            <p>– The Tappr Team</p>
            """, firstName, dashboardUrl);

        return wrapHtml(content);
    }

    public static String emailVerifiedText(String firstName, String dashboardUrl) {
        String content = String.format("""
            Hi %s,

            Your Tappr account has been successfully verified!
            You can now log in and start accepting payments instantly.

            Go to Dashboard → %s

            – The Tappr Team
            """, firstName, dashboardUrl);

        return wrapText(content);
    }

    // 🔹 Password Reset
    public static String passwordResetHtml(String firstName, String resetUrl) {
        String content = String.format("""
            <p>Hi %s,</p>
            <p>We received a request to reset your Tappr password.</p>
            <p>This link will expire in 10 minutes.</p>
            <p style="margin: 24px 0; text-align: center;">
              <a href="%s" style="background: #F59E0B; color: white; padding: 12px 20px; text-decoration: none; border-radius: 6px;">
                Reset Password
              </a>
            </p>
            <p>If you didn’t request this, please ignore this email.</p>
            <p>– The Tappr Team</p>
            """, firstName, resetUrl);

        return wrapHtml(content);
    }

    public static String passwordResetText(String firstName, String resetUrl) {
        String content = String.format("""
            Hi %s,

            We received a request to reset your Tappr password.
            This link will expire in 10 minutes.

            Reset Password → %s

            If you didn’t request this, please ignore this message.

            – The Tappr Team
            """, firstName, resetUrl);

        return wrapText(content);
    }

    // 🔹 New Login Notification
    public static String newLoginHtml(String firstName, String secureAccountUrl) {
        String content = String.format("""
            <p>Hi %s,</p>
            <p>A new sign-in to your <strong>Tappr</strong> account was detected.</p>
            <p>If this was you, no action is required. If not, please secure your account immediately.</p>
            <p style="margin: 24px 0; text-align: center;">
              <a href="%s" style="background: #DC2626; color: white; padding: 12px 20px; text-decoration: none; border-radius: 6px;">
                Secure My Account
              </a>
            </p>
            <p>– The Tappr Team</p>
            """, firstName, secureAccountUrl);

        return wrapHtml(content);
    }

    public static String newLoginText(String firstName, String secureAccountUrl) {
        String content = String.format("""
            Hi %s,

            A new sign-in to your Tappr account was detected.
            If this was you, no action is needed. If not, please secure your account immediately.

            Secure Account → %s

            – The Tappr Team
            """, firstName, secureAccountUrl);

        return wrapText(content);
    }
}
