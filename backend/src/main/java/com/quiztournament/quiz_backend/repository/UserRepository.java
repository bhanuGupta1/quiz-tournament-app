package com.quiztournament.quiz_backend.repository;

import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for User entity
 * Provides database operations and custom query methods for user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username for authentication
    Optional<User> findByUsername(String username);

    // Find user by email for authentication and password reset
    Optional<User> findByEmail(String email);

    // Check if username already exists (for validation)
    boolean existsByUsername(String username);

    // Check if email already exists (for validation)
    boolean existsByEmail(String email);

    // Find users by role (get all admins or all players)
    List<User> findByRole(UserRole role);

    // Find all users except specified role (for email notifications to players only)
    List<User> findByRoleNot(UserRole role);

    // Find users by city (using custom attribute)
    List<User> findByCity(String city);

    // Find users by preferred category
    List<User> findByPreferredCategory(String preferredCategory);
    
    // Count users by role (for statistics)
    long countByRole(UserRole role);
}