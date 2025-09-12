package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class AuthServiceTest {

    // Spring-mocked beans (registered in an ApplicationContext)
    @MockBean private UserRepository userRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private JwtUtil jwtUtil;

    // Plain Mockito mocks (not Spring beans)
    @Mock private Authentication authentication;
    @Mock private CustomUserDetailsService.CustomUserPrincipal userPrincipal;

    // SUT
    private AuthService authService;

    private User adminUser;
    private User playerUser;

    @BeforeEach
    void setUp() {
        // Manually construct the service and inject Spring mocks
        authService = new AuthService();
        // reflectively set fields OR use a ctor if you added one; here we use setters or direct field access
        // but since AuthService in your project uses @Autowired fields, call a small helper:
        inject(authService, "userRepository", userRepository);
        inject(authService, "passwordEncoder", passwordEncoder);
        inject(authService, "authenticationManager", authenticationManager);
        inject(authService, "userDetailsService", userDetailsService);
        inject(authService, "jwtUtil", jwtUtil);

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

        User result = authService.registerAdmin(newAdmin);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerAdmin_UsernameExists_ThrowsException() {
        User newAdmin = new User();
        newAdmin.setUsername("admin");
        newAdmin.setEmail("newemail@test.com");

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerAdmin(newAdmin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void registerAdmin_EmailExists_ThrowsException() {
        User newAdmin = new User();
        newAdmin.setUsername("newadmin");
        newAdmin.setEmail("admin@test.com");

        when(userRepository.existsByUsername("newadmin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerAdmin(newAdmin))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void registerPlayer_Success() {
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

        User result = authService.registerPlayer(newPlayer);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.PLAYER);
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_Success() {
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
        when(jwtUtil.generateToken(any(), any(Map.class))).thenReturn(jwtToken);

        Map<String, Object> result = authService.login(username, password);

        assertThat(result).isNotNull();
        assertThat(result.get("token")).isEqualTo(jwtToken);
        assertThat(result.get("message")).isEqualTo("Login successful");
        assertThat(result.get("user")).isNotNull();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(any(), any(Map.class));
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        String username = "admin";
        String password = "wrongpassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void createDefaultAdmin_AdminDoesNotExist_CreatesAdmin() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("op@1234")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        authService.createDefaultAdmin();

        verify(userRepository).existsByUsername("admin");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createDefaultAdmin_AdminExists_DoesNothing() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        authService.createDefaultAdmin();

        verify(userRepository).existsByUsername("admin");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findUserByUsernameOrEmail_FoundByUsername() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        Optional<User> result = authService.findUserByUsernameOrEmail("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
    }

    @Test
    void findUserByUsernameOrEmail_FoundByEmail() {
        when(userRepository.findByUsername("admin@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(adminUser));

        Optional<User> result = authService.findUserByUsernameOrEmail("admin@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void findUserByUsernameOrEmail_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = authService.findUserByUsernameOrEmail("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void isPasswordValid_CorrectPassword_ReturnsTrue() {
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);

        boolean result = authService.isPasswordValid(adminUser, "plainPassword");

        assertThat(result).isTrue();
    }

    @Test
    void isPasswordValid_IncorrectPassword_ReturnsFalse() {
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        boolean result = authService.isPasswordValid(adminUser, "wrongPassword");

        assertThat(result).isFalse();
    }

    @Test
    void updatePassword_Success() {
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(adminUser)).thenReturn(adminUser);

        authService.updatePassword(adminUser, newPassword);

        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(adminUser);
    }

    // --- tiny reflection helper to inject @Autowired fields in tests without constructors ---
    private static void inject(Object target, String fieldName, Object value) {
        try {
            var f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field: " + fieldName, e);
        }
    }
}
