package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "http://localhost:3000")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    /**
     * Basic health check
     */
    @GetMapping
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Quiz Tournament API is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Database connectivity check
     */
    @GetMapping("/db")
    public ResponseEntity<?> databaseHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test database connection
            try (Connection connection = dataSource.getConnection()) {
                response.put("database", "UP");
                response.put("connection", "OK");
            }
            
            // Test repository access
            long userCount = userRepository.count();
            response.put("userCount", userCount);
            response.put("repository", "OK");
            
            response.put("status", "UP");
            response.put("message", "Database is accessible");
            
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}