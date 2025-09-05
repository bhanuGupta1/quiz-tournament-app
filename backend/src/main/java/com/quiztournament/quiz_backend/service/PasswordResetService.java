package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling password reset functionality
 * Manages password reset tokens and verification process
 */
@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // In-memory storage for password reset tokens
    // In production, this should be stored in Redis or database
    private final Map<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();

    // Constants
    private static final int TOKEN_LENGTH = 32;
    private static final int TOKEN_EXPIRY_HOURS = 1;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Initiate password reset process
     * @param email User's email address
     * @return Reset initiation result
     */
    public Map<String, Object> initiatePasswordReset(String email) {
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email);

        Map<String, Object> response = new HashMap<>();

        if (userOptional.isEmpty()) {
            // For security, don't reveal if email exists or not
            response.put("success", true);
            response.put("message", "If an account with this email exists, a password reset link has been sent.");
            return response;
        }

        User user = userOptional.get();

        // Generate reset token
        String resetToken = generateResetToken();

        // Store token with expiry
        PasswordResetToken tokenData = new PasswordResetToken(
                user.getId(),
                user.getEmail(),
                resetToken,
                LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS)
        );

        resetTokens.put(resetToken, tokenData);

        // Clean up expired tokens
        cleanupExpiredTokens();

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(user, resetToken);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            response.put("success", false);
            response.put("error", "Failed to send password reset email");
            return response;
        }

        response.put("success", true);
        response.put("message", "If an account with this email exists, a password reset link has been sent.");
        response.put("tokenGenerated", true); // For testing purposes

        return response;
    }

    /**
     * Verify password reset token
     * @param token Reset token
     * @return Token verification result
     */
    public Map<String, Object> verifyResetToken(String token) {
        Map<String, Object> response = new HashMap<>();

        PasswordResetToken tokenData = resetTokens.get(token);

        if (tokenData == null) {
            response.put("valid", false);
            response.put("error", "Invalid or expired reset token");
            return response;
        }

        if (tokenData.isExpired()) {
            resetTokens.remove(token);
            response.put("valid", false);
            response.put("error", "Reset token has expired");
            return response;
        }

        // Find user to verify account still exists
        Optional<User> userOptional = userRepository.findById(tokenData.getUserId());
        if (userOptional.isEmpty()) {
            resetTokens.remove(token);
            response.put("valid", false);
            response.put("error", "User account not found");
            return response;
        }

        User user = userOptional.get();

        response.put("valid", true);
        response.put("email", user.getEmail());
        response.put("username", user.getUsername());
        response.put("expiresAt", tokenData.getExpiryTime());
        response.put("message", "Reset token is valid");

        return response;
    }

    /**
     * Reset password using token
     * @param token Reset token
     * @param newPassword New password
     * @return Password reset result
     */
    public Map<String, Object> resetPassword(String token, String newPassword) {
        Map<String, Object> response = new HashMap<>();

        // Verify token
        Map<String, Object> tokenVerification = verifyResetToken(token);
        if (!(Boolean) tokenVerification.getOrDefault("valid", false)) {
            return tokenVerification; // Return error from token verification
        }

        PasswordResetToken tokenData = resetTokens.get(token);

        // Find user
        User user = userRepository.findById(tokenData.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate new password
        if (newPassword == null || newPassword.trim().length() < 6) {
            response.put("success", false);
            response.put("error", "Password must be at least 6 characters long");
            return response;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove used token
        resetTokens.remove(token);

        // Clean up expired tokens
        cleanupExpiredTokens();

        response.put("success", true);
        response.put("message", "Password has been reset successfully");
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());

        return response;
    }

    /**
     * Get password reset statistics
     * @return Reset statistics
     */
    public Map<String, Object> getPasswordResetStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long activeTokens = resetTokens.values().stream()
                .filter(token -> !token.isExpired())
                .count();

        long expiredTokens = resetTokens.size() - activeTokens;

        stats.put("activeTokens", activeTokens);
        stats.put("expiredTokens", expiredTokens);
        stats.put("totalTokens", resetTokens.size());
        stats.put("tokenExpiryHours", TOKEN_EXPIRY_HOURS);

        return stats;
    }

    /**
     * Force cleanup of expired tokens
     */
    public void forceCleanupExpiredTokens() {
        int sizeBefore = resetTokens.size();
        cleanupExpiredTokens();
        int sizeAfter = resetTokens.size();

        System.out.println("Cleaned up " + (sizeBefore - sizeAfter) + " expired password reset tokens");
    }

    /**
     * Invalidate all tokens for a specific user
     * @param userId User ID
     */
    public void invalidateUserTokens(Long userId) {
        resetTokens.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId));
    }

    /**
     * Generate a secure random reset token
     */
    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return token.toString();
    }

    /**
     * Clean up expired tokens from memory
     */
    private void cleanupExpiredTokens() {
        resetTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Inner class to represent password reset token data
     */
    private static class PasswordResetToken {
        private final Long userId;
        private final String email;
        private final String token;
        private final LocalDateTime expiryTime;

        public PasswordResetToken(Long userId, String email, String token, LocalDateTime expiryTime) {
            this.userId = userId;
            this.email = email;
            this.token = token;
            this.expiryTime = expiryTime;
        }

        public Long getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return token;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}