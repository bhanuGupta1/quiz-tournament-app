package com.quiztournament.quiz_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import com.quiztournament.quiz_backend.dto.TournamentResponse;
import com.quiztournament.quiz_backend.dto.TournamentUpdateRequest;
import com.quiztournament.quiz_backend.entity.TournamentStatus;
import com.quiztournament.quiz_backend.service.TournamentService;
import com.quiztournament.quiz_backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for TournamentController
 * Tests REST endpoints with MockMvc and security
 */
@WebMvcTest(TournamentController.class)
class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TournamentService tournamentService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private TournamentCreateRequest createRequest;
    private TournamentUpdateRequest updateRequest;
    private TournamentResponse tournamentResponse;

    @BeforeEach
    void setUp() {
        createRequest = new TournamentCreateRequest();
        createRequest.setName("Test Tournament");
        createRequest.setCategory("science");
        createRequest.setDifficulty("medium");
        createRequest.setStartDate(LocalDate.now().plusDays(5));
        createRequest.setEndDate(LocalDate.now().plusDays(10));
        createRequest.setMinPassingScore(70.0);

        updateRequest = new TournamentUpdateRequest();
        updateRequest.setName("Updated Tournament Name");
        updateRequest.setStartDate(LocalDate.now().plusDays(6));
        updateRequest.setEndDate(LocalDate.now().plusDays(11));

        tournamentResponse = new TournamentResponse();
        tournamentResponse.setId(1L);
        tournamentResponse.setName("Test Tournament");
        tournamentResponse.setCategory("science");
        tournamentResponse.setDifficulty("medium");
        tournamentResponse.setStartDate(LocalDate.now().plusDays(5));
        tournamentResponse.setEndDate(LocalDate.now().plusDays(10));
        tournamentResponse.setMinPassingScore(70.0);
        tournamentResponse.setStatus(TournamentStatus.UPCOMING);
        tournamentResponse.setCreatedBy("admin");
        tournamentResponse.setCreatedAt(LocalDateTime.now());
        tournamentResponse.setParticipantCount(0L);
        tournamentResponse.setAverageScore(0.0);
        tournamentResponse.setLikeCount(0L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTournament_Success() throws Exception {
        // Given
        when(tournamentService.createTournament(any(TournamentCreateRequest.class)))
                .thenReturn(tournamentResponse);

        // When & Then
        mockMvc.perform(post("/api/tournaments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tournament created successfully"))
                .andExpect(jsonPath("$.tournament.id").value(1L))
                .andExpect(jsonPath("$.tournament.name").value("Test Tournament"))
                .andExpect(jsonPath("$.tournament.category").value("science"))
                .andExpect(jsonPath("$.tournament.difficulty").value("medium"));

        verify(tournamentService).createTournament(any(TournamentCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void createTournament_PlayerRole_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tournaments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(tournamentService, never()).createTournament(any(TournamentCreateRequest.class));
    }

    @Test
    void createTournament_NoAuthentication_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(tournamentService, never()).createTournament(any(TournamentCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTournament_ValidationError_BadRequest() throws Exception {
        // Given - Invalid request (missing name)
        TournamentCreateRequest invalidRequest = new TournamentCreateRequest();
        invalidRequest.setCategory("science");
        invalidRequest.setDifficulty("medium");
        invalidRequest.setStartDate(LocalDate.now().plusDays(5));
        invalidRequest.setEndDate(LocalDate.now().plusDays(10));
        invalidRequest.setMinPassingScore(70.0);

        // When & Then
        mockMvc.perform(post("/api/tournaments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(tournamentService, never()).createTournament(any(TournamentCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTournaments_Success() throws Exception {
        // Given
        List<TournamentResponse> tournaments = List.of(tournamentResponse);
        when(tournamentService.getAllTournaments()).thenReturn(tournaments);

        // When & Then
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournaments").isArray())
                .andExpect(jsonPath("$.tournaments[0].id").value(1L))
                .andExpect(jsonPath("$.tournaments[0].name").value("Test Tournament"))
                .andExpect(jsonPath("$.count").value(1));

        verify(tournamentService).getAllTournaments();
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void getAllTournaments_PlayerRole_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isForbidden());

        verify(tournamentService, never()).getAllTournaments();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTournamentById_Success() throws Exception {
        // Given
        when(tournamentService.getTournamentById(1L)).thenReturn(tournamentResponse);

        // When & Then
        mockMvc.perform(get("/api/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournament.id").value(1L))
                .andExpect(jsonPath("$.tournament.name").value("Test Tournament"));

        verify(tournamentService).getTournamentById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTournamentById_NotFound() throws Exception {
        // Given
        when(tournamentService.getTournamentById(999L))
                .thenThrow(new RuntimeException("Tournament not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/tournaments/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Tournament not found with id: 999"));

        verify(tournamentService).getTournamentById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTournament_Success() throws Exception {
        // Given
        TournamentResponse updatedResponse = new TournamentResponse();
        updatedResponse.setId(1L);
        updatedResponse.setName("Updated Tournament Name");
        when(tournamentService.updateTournament(eq(1L), any(TournamentUpdateRequest.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/tournaments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tournament updated successfully"))
                .andExpect(jsonPath("$.tournament.id").value(1L))
                .andExpect(jsonPath("$.tournament.name").value("Updated Tournament Name"));

        verify(tournamentService).updateTournament(eq(1L), any(TournamentUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void updateTournament_PlayerRole_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/tournaments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        verify(tournamentService, never()).updateTournament(anyLong(), any(TournamentUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTournament_Success() throws Exception {
        // Given
        doNothing().when(tournamentService).deleteTournament(1L);

        // When & Then
        mockMvc.perform(delete("/api/tournaments/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tournament deleted successfully"));

        verify(tournamentService).deleteTournament(1L);
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void deleteTournament_PlayerRole_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tournaments/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(tournamentService, never()).deleteTournament(anyLong());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTournamentsByStatus_Success() throws Exception {
        // Given
        List<TournamentResponse> upcomingTournaments = List.of(tournamentResponse);
        when(tournamentService.getTournamentsByStatus(TournamentStatus.UPCOMING))
                .thenReturn(upcomingTournaments);

        // When & Then
        mockMvc.perform(get("/api/tournaments/status/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournaments").isArray())
                .andExpect(jsonPath("$.tournaments[0].id").value(1L))
                .andExpect(jsonPath("$.status").value("upcoming"))
                .andExpect(jsonPath("$.count").value(1));

        verify(tournamentService).getTournamentsByStatus(TournamentStatus.UPCOMING);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTournamentsByStatus_InvalidStatus_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tournaments/status/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid status. Valid statuses are: upcoming, ongoing, past"));

        verify(tournamentService, never()).getTournamentsByStatus(any(TournamentStatus.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMyTournaments_Success() throws Exception {
        // Given
        List<TournamentResponse> myTournaments = List.of(tournamentResponse);
        when(tournamentService.getMyTournaments()).thenReturn(myTournaments);

        // When & Then
        mockMvc.perform(get("/api/tournaments/my-tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournaments").isArray())
                .andExpect(jsonPath("$.tournaments[0].id").value(1L))
                .andExpect(jsonPath("$.count").value(1));

        verify(tournamentService).getMyTournaments();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTournamentsByCategory_Success() throws Exception {
        // Given
        List<TournamentResponse> scienceTournaments = List.of(tournamentResponse);
        when(tournamentService.getTournamentsByCategory("science")).thenReturn(scienceTournaments);

        // When & Then
        mockMvc.perform(get("/api/tournaments/category/science"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournaments").isArray())
                .andExpect(jsonPath("$.category").value("science"))
                .andExpect(jsonPath("$.count").value(1));

        verify(tournamentService).getTournamentsByCategory("science");
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTournamentsByDifficulty_Success() throws Exception {
        // Given
        List<TournamentResponse> mediumTournaments = List.of(tournamentResponse);
        when(tournamentService.getTournamentsByDifficulty("medium")).thenReturn(mediumTournaments);

        // When & Then
        mockMvc.perform(get("/api/tournaments/difficulty/medium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tournaments").isArray())
                .andExpect(jsonPath("$.difficulty").value("medium"))
                .andExpect(jsonPath("$.count").value(1));

        verify(tournamentService).getTournamentsByDifficulty("medium");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTournamentStatistics_Success() throws Exception {
        // Given
        when(tournamentService.getTotalTournamentCount()).thenReturn(5L);
        when(tournamentService.getTournamentsByStatus(TournamentStatus.UPCOMING)).thenReturn(List.of(tournamentResponse));
        when(tournamentService.getTournamentsByStatus(TournamentStatus.ONGOING)).thenReturn(List.of());
        when(tournamentService.getTournamentsByStatus(TournamentStatus.PAST)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/tournaments/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalTournaments").value(5L))
                .andExpect(jsonPath("$.upcomingCount").value(1))
                .andExpect(jsonPath("$.ongoingCount").value(0))
                .andExpect(jsonPath("$.pastCount").value(0));

        verify(tournamentService).getTotalTournamentCount();
        verify(tournamentService, times(3)).getTournamentsByStatus(any(TournamentStatus.class));
    }

    @Test
    void healthCheck_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/tournaments/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tournament service is running"));
    }
}