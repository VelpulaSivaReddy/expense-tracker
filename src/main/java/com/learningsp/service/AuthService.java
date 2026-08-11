package com.learningsp.service;

import com.learningsp.dto.auth.*;
import com.learningsp.entity.PasswordResetToken;
import com.learningsp.entity.User;
import com.learningsp.exception.BadRequestException;
import com.learningsp.exception.DuplicateResourceException;
import com.learningsp.exception.ResourceNotFoundException;
import com.learningsp.repo.PasswordResetTokenRepository;
import com.learningsp.repo.UserRepository;
import com.learningsp.util.JwtService;
import com.learningsp.util.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Value("${app.password-reset.token-expiration-minutes:30}")
    private int tokenExpirationMinutes;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        User saved = userRepository.save(user);
        
        // Send welcome email
        try {
            emailService.sendWelcomeEmail(saved.getEmail(), saved.getFullName());
        } catch (Exception e) {
            // Log but don't fail registration if email fails
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
        
        UserPrincipal principal = new UserPrincipal(saved);
        String token = jwtService.generateToken(principal, saved.getUserId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(saved.getUserId())
                .fullName(saved.getFullName())
                .email(saved.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal, user.getUserId());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // IMPORTANT: never reveal whether an email is registered. Every call returns the same
        // generic outcome to the caller (see AuthController), regardless of whether a matching
        // account exists, so this method must never throw for an unknown email.
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);

            // Save token to database
            PasswordResetToken token = PasswordResetToken.builder()
                    .token(resetToken)
                    .userId(user.getUserId())
                    .expiresAt(expiresAt)
                    .used(false)
                    .build();
            resetTokenRepository.save(token);

            // Build reset link for frontend
            String resetLink = frontendUrl + "/reset-password.html?token=" + resetToken;

            // Send email with reset link
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken, resetLink);
            } catch (Exception e) {
                // Clean up the token if email fails. We still don't leak this to the caller
                // (see AuthController) to avoid distinguishing "unknown email" from "email send failed".
                resetTokenRepository.delete(token);
            }
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Find and validate token
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (!resetToken.isValid()) {
            throw new BadRequestException("Reset token has expired or already been used");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /** Housekeeping: purge expired/used password-reset tokens so the table doesn't grow forever. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredResetTokens() {
        resetTokenRepository.deleteExpiredOrUsedTokens(LocalDateTime.now());
    }
}
