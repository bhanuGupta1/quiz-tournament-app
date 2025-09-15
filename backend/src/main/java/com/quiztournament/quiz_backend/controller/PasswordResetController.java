package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.dto.ForgotPasswordRequest;
import com.quiztournament.quiz_backend.dto.ResetPasswordRequest;
import com.quiztournament.quiz_backend.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Password Reset functionality
 * Handles password reset requests, token verification, and password updates
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Request password reset (Public endpoint)
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            Map<String, Object> result = passwordResetService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "An error occurred while processing your request");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Verify reset token (Public endpoint)
     * GET /api/auth/verify-reset-token
     */
    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyResetToken(@RequestParam String token) {
        try {
            Map<String, Object> result = passwordResetService.verifyResetToken(token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", "An error occurred while verifying the token");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Reset password using token (Public endpoint)
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            Map<String, Object> result = passwordResetService.resetPassword(
                    request.getToken(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "An error occurred while resetting the password");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get password reset statistics (Admin only)
     * GET /api/auth/password-reset-stats
     */
    @GetMapping("/password-reset-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPasswordResetStatistics() {
        try {
            Map<String, Object> statistics = passwordResetService.getPasswordResetStatistics();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("statistics", statistics);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Force cleanup of expired tokens (Admin only)
     * POST /api/auth/cleanup-reset-tokens
     */
    @PostMapping("/cleanup-reset-tokens")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceCleanupExpiredTokens() {
        try {
            passwordResetService.forceCleanupExpiredTokens();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("message", "Expired password reset tokens cleaned up successfully");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check for password reset service
     * GET /api/auth/password-reset/health
     */
    @GetMapping("/password-reset/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password Reset service is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // Inner classes for request DTOs

    /**
     * DTO for forgot password request
     */
    public static class ForgotPasswordRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        public ForgotPasswordRequest() {}

        public ForgotPasswordRequest(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * DTO for reset password request
     */
    public static class ResetPasswordRequest {

        @NotBlank(message = "Reset token is required")
        private String token;

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;

        public ResetPasswordRequest() {}

        public ResetPasswordRequest(String token, String newPassword) {
            this.token = token;
            this.newPassword = newPassword;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}