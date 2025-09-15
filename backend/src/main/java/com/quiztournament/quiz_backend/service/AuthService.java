package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling user authentication operations
 * Manages user registration, login, and token generation
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Use interface instead of concrete class for better testability
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new admin user
     * @param user User details for registration
     * @return Registered user with encoded password
     */
    public User registerAdmin(User user) {
        // Validate username and email are unique
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Set role and encode password
        user.setRole(UserRole.ADMIN);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * Register a new player user
     * @param user User details for registration
     * @return Registered user with encoded password
     */
    public User registerPlayer(User user) {
        // Validate username and email are unique
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Set role and encode password
        user.setRole(UserRole.PLAYER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * Authenticate user and generate JWT token
     * @param username Username or email
     * @param password Plain text password
     * @return Authentication response with token and user info
     */
    public Map<String, Object> login(String username, String password) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                    (CustomUserDetailsService.CustomUserPrincipal) userDetails;

            // Generate JWT token with user role
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", userPrincipal.getRole());
            claims.put("userId", userPrincipal.getId());
            claims.put("email", userPrincipal.getEmail());

            String token = jwtUtil.generateToken(userDetails, claims);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", createUserResponse(userPrincipal.getUser()));
            response.put("message", "Login successful");

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    /**
     * Create default admin user (admin/op@1234) if it doesn't exist
     */
    public void createDefaultAdmin() {
        try {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setEmail("admin@quiztournament.com");
                admin.setPassword("op@1234");
                admin.setRole(UserRole.ADMIN);

                registerAdmin(admin);
                System.out.println("Default admin user created - Username: admin, Password: op@1234");
            } else {
                System.out.println("Default admin user already exists");
            }
        } catch (Exception e) {
            System.err.println("Failed to create default admin user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Create simple test users for easy testing
     */
    public void createTestUsers() {
        try {
            // Note: Using existing admin account (admin/op@1234) - no duplicate admin created

            // Create simple player user
            if (!userRepository.existsByUsername("user")) {
                User player = new User();
                player.setUsername("user");
                player.setFirstName("Test");
                player.setLastName("Player");
                player.setEmail("user@test.com");
                player.setPassword("user");
                player.setRole(UserRole.PLAYER);
                registerPlayer(player);
                System.out.println("Test player user created - Username: user, Password: user");
            }

            // Create additional test players
            if (!userRepository.existsByUsername("player1")) {
                User player1 = new User();
                player1.setUsername("player1");
                player1.setFirstName("John");
                player1.setLastName("Doe");
                player1.setEmail("john@test.com");
                player1.setPassword("password");
                player1.setRole(UserRole.PLAYER);
                registerPlayer(player1);
                System.out.println("Test player1 user created - Username: player1, Password: password");
            }

            if (!userRepository.existsByUsername("player2")) {
                User player2 = new User();
                player2.setUsername("player2");
                player2.setFirstName("Jane");
                player2.setLastName("Smith");
                player2.setEmail("jane@test.com");
                player2.setPassword("password");
                player2.setRole(UserRole.PLAYER);
                registerPlayer(player2);
                System.out.println("Test player2 user created - Username: player2, Password: password");
            }

        } catch (Exception e) {
            System.err.println("Failed to create test users: " + e.getMessage());
            // Don't throw exception to avoid breaking application startup
            e.printStackTrace();
        }
    }

    /**
     * Update user profile
     * @param userId User ID
     * @param profileUpdateRequest Updated profile information
     * @return Updated user
     */
    public User updateProfile(Long userId, com.quiztournament.quiz_backend.dto.ProfileUpdateRequest profileUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username is being changed and if it's already taken by another user
        if (!user.getUsername().equals(profileUpdateRequest.getUsername())) {
            if (userRepository.existsByUsername(profileUpdateRequest.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
        }

        // Check if email is being changed and if it's already taken by another user
        if (!user.getEmail().equals(profileUpdateRequest.getEmail())) {
            if (userRepository.existsByEmail(profileUpdateRequest.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
        }

        // Update user fields
        user.setUsername(profileUpdateRequest.getUsername());
        user.setFirstName(profileUpdateRequest.getFirstName());
        user.setLastName(profileUpdateRequest.getLastName());
        user.setEmail(profileUpdateRequest.getEmail());
        user.setPhoneNumber(profileUpdateRequest.getPhoneNumber());
        user.setCity(profileUpdateRequest.getCity());
        user.setPreferredCategory(profileUpdateRequest.getPreferredCategory());

        return userRepository.save(user);
    }

    /**
     * Find user by username or email
     * @param identifier Username or email
     * @return User if found
     */
    public Optional<User> findUserByUsernameOrEmail(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier));
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
     * Validate if current password is correct
     * @param user User entity
     * @param rawPassword Plain text password
     * @return true if password matches
     */
    public boolean isPasswordValid(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /**
     * Update user password
     * @param user User entity
     * @param newPassword New plain text password
     */
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}