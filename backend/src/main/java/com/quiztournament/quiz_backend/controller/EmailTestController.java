package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Email Service Testing
 * Provides endpoints to test and monitor email functionality
 */
@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {"http://localhost:3000", "https://majestic-tarsier-882abf.netlify.app"})
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    /**
     * Get email service status and configuration
     * GET /api/email/status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEmailServiceStatus() {
        try {
            Map<String, Object> status = emailService.getEmailServiceStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("emailService", status);
            response.put("message", "Email service status retrieved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Test email connectivity
     * POST /api/email/test-connectivity
     */
    @PostMapping("/test-connectivity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testEmailConnectivity() {
        try {
            boolean isWorking = emailService.testEmailConnectivity();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("emailWorking", isWorking);
            response.put("configured", isWorking);
            response.put("message", isWorking ? 
                "Email service is configured and working" : 
                "Email service is disabled or not configured (demo mode)");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("emailWorking", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check for email service
     * GET /api/email/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Email service endpoint is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        response.put("service", "EmailTestController");
        return ResponseEntity.ok(response);
    }
}