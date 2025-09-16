package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.dto.LoginRequest;
import com.quiztournament.quiz_backend.dto.ProfileUpdateRequest;
import com.quiztournament.quiz_backend.dto.RegisterRequest;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for authentication operations
 * Handles user login, registration, and authentication-related endpoints
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private com.quiztournament.quiz_backend.repository.QuizResultRepository quizResultRepository;

    @Autowired
    private com.quiztournament.quiz_backend.repository.UserRepository userRepository;

    /**
     * User login endpoint
     * @param loginRequest Login credentials
     * @return JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Map<String, Object> response = authService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Register new admin user
     * @param registerRequest User registration details
     * @return Success message with user info
     */
    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = createUserFromRequest(registerRequest);
            User registeredUser = authService.registerAdmin(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin user registered successfully");
            response.put("user", createUserResponse(registeredUser));
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Register new player user
     * @param registerRequest User registration details
     * @return Success message with user info
     */
    @PostMapping("/register/player")
    public ResponseEntity<?> registerPlayer(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = createUserFromRequest(registerRequest);
            User registeredUser = authService.registerPlayer(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Player user registered successfully");
            response.put("user", createUserResponse(registeredUser));
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Test endpoint to verify authentication is working
     * @return Simple success message
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Authentication service is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Convert RegisterRequest to User entity
     * @param request Registration request
     * @return User entity
     */
    private User createUserFromRequest(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Will be encoded in service
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCity(request.getCity());
        user.setPreferredCategory(request.getPreferredCategory());
        user.setPicture(request.getPicture());
        return user;
    }

    /**
     * Create safe user response (without password)
     * @param user User entity
     * @return Map with safe user data
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("email", user.getEmail());
        userResponse.put("role", user.getRole().name());
        userResponse.put("phoneNumber", user.getPhoneNumber());
        userResponse.put("city", user.getCity());
        userResponse.put("preferredCategory", user.getPreferredCategory());
        userResponse.put("picture", user.getPicture());
        return userResponse;
    }

    /**
     * Update user profile endpoint
     * @param profileUpdateRequest Updated profile information
     * @return Updated user information
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not authenticated");
                errorResponse.put("success", false);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

            User updatedUser = authService.updateProfile(userPrincipal.getId(), profileUpdateRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("user", createUserResponse(updatedUser));
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get current user's statistics
     * @return User's tournament participation statistics
     */
    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStatistics() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not authenticated");
                errorResponse.put("success", false);
                return ResponseEntity.status(401).body(errorResponse);
            }

            com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

            User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Calculate user statistics - REAL DATA ONLY
            long tournamentsParticipated = quizResultRepository.countByUser(user);
            Double averageScore = quizResultRepository.findAveragePercentageByUser(user);
            if (averageScore == null) averageScore = 0.0;

            Map<String, Object> response = new HashMap<>();
            response.put("tournamentsParticipated", tournamentsParticipated);
            response.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
            response.put("city", user.getCity() != null ? user.getCity() : "Not set");
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}