package com.learningsp.service;

import com.learningsp.config.EmailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;

    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken, String resetLink) throws Exception {
        try {
            log.debug("Preparing password reset email for: {}", toEmail);
            
            if (emailConfig.getFrom() == null || emailConfig.getFrom().isEmpty()) {
                throw new IllegalArgumentException("Email 'from' address is not configured. Check MAIL_FROM in .env file.");
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom(), emailConfig.getFromName());
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Expense Tracker");

            String htmlContent = buildPasswordResetEmailHtml(fullName, resetToken, resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Messaging error sending password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to prepare email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {} | Cause: {}", toEmail, e.getMessage(), e.getCause());
            throw e;
        }
    }

    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getFrom());
            message.setTo(toEmail);
            message.setSubject("Welcome to Expense Tracker!");
            message.setText(buildWelcomeEmailText(fullName));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildPasswordResetEmailHtml(String fullName, String resetToken, String resetLink) {
        return "<html>" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "<h2>Password Reset Request</h2>" +
                "<p>Hi <strong>" + fullName + "</strong>,</p>" +
                "<p>We received a request to reset your password. Click the button below to proceed:</p>" +
                "<div style=\"text-align: center; margin: 30px 0;\">" +
                "<a href=\"" + resetLink + "\" style=\"background-color: #007bff; color: white; padding: 12px 30px; " +
                "text-decoration: none; border-radius: 5px; display: inline-block;\">Reset Password</a>" +
                "</div>" +
                "<p style=\"color: #666; font-size: 14px;\">Or copy this reset token and use it in your app:</p>" +
                "<p style=\"background-color: #f5f5f5; padding: 10px; border-radius: 5px; word-break: break-all;\">" +
                "<code>" + resetToken + "</code></p>" +
                "<p style=\"color: #999; font-size: 12px;\">This link will expire in 30 minutes.</p>" +
                "<p style=\"color: #999; font-size: 12px;\">If you didn't request this, please ignore this email.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private String buildWelcomeEmailText(String fullName) {
        return "Welcome to Expense Tracker, " + fullName + "!\n\n" +
                "Your account has been created successfully. You can now log in and start tracking your expenses.\n\n" +
                "Features available:\n" +
                "- Track your daily expenses\n" +
                "- Categorize and filter expenses\n" +
                "- Set and monitor budgets\n" +
                "- Generate detailed reports\n" +
                "- Export data to Excel and PDF\n\n" +
                "Happy expense tracking!\n\n" +
                "Best regards,\n" +
                "The Expense Tracker Team";
    }
}
