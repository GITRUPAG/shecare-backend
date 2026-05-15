package com.app.shecare.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.shecare.entity.PasswordResetToken;
import com.app.shecare.entity.User;
import com.app.shecare.repository.PasswordResetTokenRepository;
import com.app.shecare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository               userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService                 emailService;
    private final PasswordEncoder              passwordEncoder;

    @Value("${app_frontend_url:https://shecare.fit}")
    private String frontendUrl;

    private static final int EXPIRY_MINUTES = 15;

    // ─── STEP 1: User requests reset ─────────────────────────────────────────
    public void requestReset(String email) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return; // silently do nothing — never reveal if email exists

        // Delete any existing tokens for this user
        tokenRepository.deleteAllByUserId(user.getId());

        // Store raw token — safe for MVP (expires in 15 min, single-use)
        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(rawToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
    }

    // ─── STEP 2: Validate token ───────────────────────────────────────────────
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        return tokenRepository.findByToken(token.trim())
                .map(t -> !t.isExpired() && !t.getUsed())
                .orElse(false);
    }

    // ─── STEP 3: Reset the password ──────────────────────────────────────────
    public void resetPassword(String token, String newPassword) {

        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters.");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link."));

        if (resetToken.isExpired()) {
            throw new RuntimeException("This reset link has expired. Please request a new one.");
        }

        if (resetToken.getUsed()) {
            throw new RuntimeException("This reset link has already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}