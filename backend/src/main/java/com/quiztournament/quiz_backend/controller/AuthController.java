package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.dto.LoginRequest;
import com.quiztournament.quiz_backend.dto.RegisterRequest;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
}