package com.quiztournament.quiz_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiztournament.quiz_backend.dto.LoginRequest;
import com.quiztournament.quiz_backend.dto.RegisterRequest;
import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import com.quiztournament.quiz_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * Integration tests for the complete Quiz Tournament application
 * Tests end-to-end workflows and system integration
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class QuizTournamentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String playerToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create default admin user programmatically
        User admin = new User();
        admin.setUsername("admin");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("op@1234"));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);

        // Login as admin to get token
        LoginRequest adminLogin = new LoginRequest("admin", "op@1234");
        MvcResult adminResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> adminResponse = objectMapper.readValue(
                adminResult.getResponse().getContentAsString(), Map.class);
        adminToken = (String) adminResponse.get("token");
    }

    @Test
    @Transactional
    void completeWorkflow_PlayerRegistrationToQuizCompletion() throws Exception {
        // Step 1: Register a new player
        RegisterRequest playerRegister = new RegisterRequest();
        playerRegister.setUsername("testplayer");
        playerRegister.setFirstName("Test");
        playerRegister.setLastName("Player");
        playerRegister.setEmail("player@test.com");
        playerRegister.setPassword("password123");
        playerRegister.setCity("Auckland");
        playerRegister.setPreferredCategory("science");

        mockMvc.perform(post("/api/auth/register/player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerRegister)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Player user registered successfully"));

        // Step 2: Login as player
        LoginRequest playerLogin = new LoginRequest("testplayer", "password123");
        MvcResult playerResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        Map<String, Object> playerResponse = objectMapper.readValue(
                playerResult.getResponse().getContentAsString(), Map.class);
        playerToken = (String) playerResponse.get("token");

        // Step 3: Admin creates a tournament
        TournamentCreateRequest tournamentRequest = new TournamentCreateRequest();
        tournamentRequest.setName("Integration Test Tournament");
        tournamentRequest.setCategory("science");
        tournamentRequest.setDifficulty("medium");
        tournamentRequest.setStartDate(LocalDate.now());
        tournamentRequest.setEndDate(LocalDate.now().plusDays(7));
        tournamentRequest.setMinPassingScore(70.0);

        MvcResult tournamentResult = mockMvc.perform(post("/api/tournaments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tournamentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournament.name").value("Integration Test Tournament"))
                .andReturn();

        Map<String, Object> tournamentResponse = objectMapper.readValue(
                tournamentResult.getResponse().getContentAsString(), Map.class);
        Map<String, Object> tournament = (Map<String, Object>) tournamentResponse.get("tournament");
        Integer tournamentId = (Integer) tournament.get("id");

        // Step 4: Player checks tournament eligibility
        mockMvc.perform(get("/api/participation/tournaments/" + tournamentId + "/eligibility")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.canParticipate").value(true));

        // Step 5: Player likes the tournament
        mockMvc.perform(post("/api/tournaments/" + tournamentId + "/like")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userLiked").value(true));

        // Step 6: Check like status
        mockMvc.perform(get("/api/tournaments/" + tournamentId + "/like-status")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLiked").value(true))
                .andExpect(jsonPath("$.totalLikes").value(1));

        // Step 7: Player starts quiz participation
        mockMvc.perform(post("/api/participation/tournaments/" + tournamentId + "/start")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournamentName").value("Integration Test Tournament"));

        // Step 8: Player gets tournament questions
        mockMvc.perform(get("/api/tournaments/" + tournamentId + "/questions")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalQuestions").value(10))
                .andExpect(jsonPath("$.questions").isArray());

        // Step 9: Player views quiz history (should be empty before completion)
        mockMvc.perform(get("/api/participation/my-quiz-history")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalParticipated").value(0));

        // Step 10: Admin views tournament statistics
        mockMvc.perform(get("/api/participation/tournaments/" + tournamentId + "/statistics")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statistics.totalParticipants").value(0));

        // Step 11: Check most popular tournaments
        mockMvc.perform(get("/api/tournaments/popular?limit=5")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.popularTournaments").isArray())
                .andExpect(jsonPath("$.popularTournaments[0].likeCount").value(1));

        // Step 12: Admin gets all tournaments
        mockMvc.perform(get("/api/tournaments")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.tournaments[0].name").value("Integration Test Tournament"));
    }

    @Test
    void passwordResetWorkflow() throws Exception {
        // Step 1: Create a player for password reset test
        RegisterRequest playerRegister = new RegisterRequest();
        playerRegister.setUsername("resetplayer");
        playerRegister.setFirstName("Reset");
        playerRegister.setLastName("Player");
        playerRegister.setEmail("reset@test.com");
        playerRegister.setPassword("oldpassword");

        mockMvc.perform(post("/api/auth/register/player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(playerRegister)))
                .andExpect(status().isOk());

        // Step 2: Request password reset
        Map<String, String> resetRequest = Map.of("email", "reset@test.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        // Note: In a real integration test, you would need to:
        // 1. Extract the reset token from logs or email service mock
        // 2. Verify the token
        // 3. Reset the password
        // 4. Login with new password
        // For this example, we're just testing the request is accepted
    }

    @Test
    void unauthorizedAccessTest() throws Exception {
        // Test admin endpoints without authentication
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // Test player endpoints without authentication
        mockMvc.perform(get("/api/participation/my-quiz-history"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tournaments/1/like"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthCheckEndpoints() throws Exception {
        // Test various health check endpoints
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Authentication service is working"));

        mockMvc.perform(get("/api/tournaments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tournament service is running"));

        mockMvc.perform(get("/api/tournaments/questions/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Question service is running"));

        mockMvc.perform(get("/api/participation/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Quiz Participation service is running"));
    }

    @Test
    void apiDocumentationAccessible() throws Exception {
        // Test that Swagger UI is accessible
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());

        // Test that OpenAPI JSON is accessible
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}