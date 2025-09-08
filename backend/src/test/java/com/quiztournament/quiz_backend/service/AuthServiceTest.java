package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 * Tests user authentication, registration, and token generation
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetailsService.CustomUserPrincipal userPrincipal;

    @InjectMocks
    private AuthService authService;

    private User adminUser;
    private User playerUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setRole(UserRole.ADMIN);

        playerUser = new User();
        playerUser.setId(2L);
        playerUser.setUsername("player");
        playerUser.setFirstName("Player");
        playerUser.setLastName("User");
        playerUser.setEmail("player@test.com");
        playerUser.setPassword("encodedPassword");
        playerUser.setRole(UserRole.PLAYER);
    }

    @Test
    void registerAdmin_Success() {
        // Given
        User newAdmin = new User();
        newAdmin.setUsername("newadmin");
        newAdmin.setFirstName("New");
        newAdmin.setLastName("Admin");
        newAdmin.setEmail("newadmin@test.com");
        newAdmin.setPassword("plainPassword");

        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(userRepository.existsByEmail("newadmin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        // When
        User result = authService.registerAdmin(newAdmin);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerAdmin_UsernameExists_ThrowsException() {
        // Given
        User newAdmin = new User();
        newAdmin.setUsername("admin");
        newAdmin.setEmail("newemail@test.com");

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerAdmin(newAdmin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void registerAdmin_EmailExists_ThrowsException() {
        // Given
        User newAdmin = new User();
        newAdmin.setUsername("newadmin");
        newAdmin.setEmail("admin@test.com");

        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerAdmin(newAdmin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void registerPlayer_Success() {
        // Given
        User newPlayer = new User();
        newPlayer.setUsername("newplayer");
        newPlayer.setFirstName("New");
        newPlayer.setLastName("Player");
        newPlayer.setEmail("newplayer@test.com");
        newPlayer.setPassword("plainPassword");

        when(userRepository.existsByUsername("newplayer")).thenReturn(false);
        when(userRepository.existsByEmail("newplayer@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(playerUser);

        // When
        User result = authService.registerPlayer(newPlayer);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.PLAYER);
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Given
        String username = "admin";
        String password = "password";
        String jwtToken = "jwt.token.here";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userPrincipal);
        when(userPrincipal.getRole()).thenReturn("ADMIN");
        when(userPrincipal.getId()).thenReturn(1L);
        when(userPrincipal.getEmail()).thenReturn("admin@test.com");
        when(userPrincipal.getUser()).thenReturn(adminUser);
        when(jwtUtil.generateToken(eq(userPrincipal), any(Map.class))).thenReturn(jwtToken);

        // When
        Map<String, Object> result = authService.login(username, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get("token")).isEqualTo(jwtToken);
        assertThat(result.get("message")).isEqualTo("Login successful");
        assertThat(result.get("user")).isNotNull();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(eq(userPrincipal), any(Map.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        // Given
        String username = "admin";
        String password = "wrongpassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void createDefaultAdmin_AdminDoesNotExist_CreatesAdmin() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("op@1234")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        // When
        authService.createDefaultAdmin();

        // Then
        verify(userRepository).existsByUsername("admin");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createDefaultAdmin_AdminExists_DoesNothing() {
        // Given
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        // When
        authService.createDefaultAdmin();

        // Then
        verify(userRepository).existsByUsername("admin");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findUserByUsernameOrEmail_FoundByUsername() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // When
        Optional<User> result = authService.findUserByUsernameOrEmail("admin");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
    }

    @Test
    void findUserByUsernameOrEmail_FoundByEmail() {
        // Given
        when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        // When
        Optional<User> result = authService.findUserByUsernameOrEmail("admin@test.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void findUserByUsernameOrEmail_NotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = authService.findUserByUsernameOrEmail("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void isPasswordValid_CorrectPassword_ReturnsTrue() {
        // Given
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);

        // When
        boolean result = authService.isPasswordValid(adminUser, "plainPassword");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isPasswordValid_IncorrectPassword_ReturnsFalse() {
        // Given
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When
        boolean result = authService.isPasswordValid(adminUser, "wrongPassword");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void updatePassword_Success() {
        // Given
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(adminUser)).thenReturn(adminUser);

        // When
        authService.updatePassword(adminUser, newPassword);

        // Then
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(adminUser);
    }
}