package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import com.quiztournament.quiz_backend.dto.TournamentResponse;
import com.quiztournament.quiz_backend.dto.TournamentUpdateRequest;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.TournamentStatus;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import com.quiztournament.quiz_backend.repository.TournamentRepository;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TournamentService
 * Tests tournament CRUD operations, validation, and business logic
 */
@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserPrincipal userPrincipal;

    @InjectMocks
    private TournamentService tournamentService;

    private User adminUser;
    private User playerUser;
    private Tournament tournament;
    private TournamentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // Create test admin user
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(UserRole.ADMIN);

        // Create test player user
        playerUser = new User();
        playerUser.setId(2L);
        playerUser.setUsername("player");
        playerUser.setFirstName("Player");
        playerUser.setLastName("User");
        playerUser.setEmail("player@test.com");
        playerUser.setRole(UserRole.PLAYER);

        // Create test tournament
        tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("Test Tournament");
        tournament.setCategory("science");
        tournament.setDifficulty("medium");
        tournament.setStartDate(LocalDate.now().plusDays(1));
        tournament.setEndDate(LocalDate.now().plusDays(7));
        tournament.setMinPassingScore(70.0);
        tournament.setCreatedBy(adminUser);

        // Create test request
        createRequest = new TournamentCreateRequest();
        createRequest.setName("New Tournament");
        createRequest.setCategory("history");
        createRequest.setDifficulty("easy");
        createRequest.setStartDate(LocalDate.now().plusDays(2));
        createRequest.setEndDate(LocalDate.now().plusDays(8));
        createRequest.setMinPassingScore(60.0);
    }

    @Test
    void createTournament_Success() {
        // Given
        when(userPrincipal.getId()).thenReturn(adminUser.getId());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
        doNothing().when(emailService).sendNewTournamentNotification(any(Tournament.class), eq(adminUser));

        // Mock security context
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            // When
            TournamentResponse response = tournamentService.createTournament(createRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo(tournament.getName());
            assertThat(response.getCategory()).isEqualTo(tournament.getCategory());
            assertThat(response.getDifficulty()).isEqualTo(tournament.getDifficulty());

            verify(tournamentRepository).save(any(Tournament.class));
            verify(emailService).sendNewTournamentNotification(any(Tournament.class), eq(adminUser));
        }
    }

    @Test
    void createTournament_NonAdminUser_ThrowsException() {
        // Given
        User nonAdminUser = new User();
        nonAdminUser.setId(3L);
        nonAdminUser.setRole(UserRole.PLAYER);

        when(userPrincipal.getId()).thenReturn(nonAdminUser.getId());
        when(userRepository.findById(nonAdminUser.getId())).thenReturn(Optional.of(nonAdminUser));

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            // When & Then
            assertThatThrownBy(() -> tournamentService.createTournament(createRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Only admin users can create tournaments");
        }
    }

    @Test
    void getAllTournaments_Success() {
        // Given
        List<Tournament> tournaments = List.of(tournament);
        when(tournamentRepository.findAll(any(Sort.class))).thenReturn(tournaments);
        when(tournamentRepository.countParticipants(anyLong())).thenReturn(5L);
        when(tournamentRepository.getAverageScore(anyLong())).thenReturn(75.5);
        when(tournamentRepository.countLikes(anyLong())).thenReturn(3L);

        // When
        List<TournamentResponse> responses = tournamentService.getAllTournaments();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo(tournament.getName());
        assertThat(responses.get(0).getParticipantCount()).isEqualTo(5L);
        assertThat(responses.get(0).getAverageScore()).isEqualTo(75.5);
        assertThat(responses.get(0).getLikeCount()).isEqualTo(3L);
    }

    @Test
    void getTournamentById_Success() {
        // Given
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.countParticipants(1L)).thenReturn(10L);
        when(tournamentRepository.getAverageScore(1L)).thenReturn(80.0);
        when(tournamentRepository.countLikes(1L)).thenReturn(5L);

        // When
        TournamentResponse response = tournamentService.getTournamentById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo(tournament.getName());
        assertThat(response.getParticipantCount()).isEqualTo(10L);
    }

    @Test
    void getTournamentById_NotFound_ThrowsException() {
        // Given
        when(tournamentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tournamentService.getTournamentById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tournament not found with id: 999");
    }

    @Test
    void updateTournament_Success() {
        // Given
        TournamentUpdateRequest updateRequest = new TournamentUpdateRequest();
        updateRequest.setName("Updated Tournament Name");
        updateRequest.setStartDate(LocalDate.now().plusDays(3));
        updateRequest.setEndDate(LocalDate.now().plusDays(9));

        Tournament updatedTournament = new Tournament();
        updatedTournament.setId(1L);
        updatedTournament.setName("Updated Tournament Name");
        updatedTournament.setCategory(tournament.getCategory());
        updatedTournament.setDifficulty(tournament.getDifficulty());
        updatedTournament.setStartDate(updateRequest.getStartDate());
        updatedTournament.setEndDate(updateRequest.getEndDate());
        updatedTournament.setMinPassingScore(tournament.getMinPassingScore());
        updatedTournament.setCreatedBy(adminUser);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userPrincipal.getId()).thenReturn(adminUser.getId());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(updatedTournament);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            // When
            TournamentResponse response = tournamentService.updateTournament(1L, updateRequest);

            // Then
            assertThat(response).isNotNull();
            verify(tournamentRepository).save(any(Tournament.class));
        }
    }

    @Test
    void deleteTournament_Success() {
        // Given
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userPrincipal.getId()).thenReturn(adminUser.getId());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(tournamentRepository.countParticipants(1L)).thenReturn(0L);
        doNothing().when(tournamentRepository).delete(tournament);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            // When
            tournamentService.deleteTournament(1L);

            // Then
            verify(tournamentRepository).delete(tournament);
        }
    }

    @Test
    void deleteTournament_WithParticipants_ThrowsException() {
        // Given
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(userPrincipal.getId()).thenReturn(adminUser.getId());
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(tournamentRepository.countParticipants(1L)).thenReturn(5L);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);

            // When & Then
            assertThatThrownBy(() -> tournamentService.deleteTournament(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cannot delete tournament with existing participants");
        }
    }

    @Test
    void getTournamentsByStatus_UpcomingTournaments() {
        // Given
        List<Tournament> upcomingTournaments = List.of(tournament);
        when(tournamentRepository.findUpcomingTournaments(any(LocalDate.class))).thenReturn(upcomingTournaments);
        when(tournamentRepository.countParticipants(anyLong())).thenReturn(0L);
        when(tournamentRepository.getAverageScore(anyLong())).thenReturn(null);
        when(tournamentRepository.countLikes(anyLong())).thenReturn(2L);

        // When
        List<TournamentResponse> responses = tournamentService.getTournamentsByStatus(TournamentStatus.UPCOMING);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(TournamentStatus.UPCOMING);
        verify(tournamentRepository).findUpcomingTournaments(any(LocalDate.class));
    }

    @Test
    void getTournamentsByCategory_Success() {
        // Given
        List<Tournament> scienceTournaments = List.of(tournament);
        when(tournamentRepository.findByCategory("science")).thenReturn(scienceTournaments);
        when(tournamentRepository.countParticipants(anyLong())).thenReturn(8L);
        when(tournamentRepository.getAverageScore(anyLong())).thenReturn(72.3);
        when(tournamentRepository.countLikes(anyLong())).thenReturn(4L);

        // When
        List<TournamentResponse> responses = tournamentService.getTournamentsByCategory("science");

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategory()).isEqualTo("science");
        assertThat(responses.get(0).getParticipantCount()).isEqualTo(8L);
    }

    @Test
    void getTournamentsByDifficulty_Success() {
        // Given
        List<Tournament> mediumTournaments = List.of(tournament);
        when(tournamentRepository.findByDifficulty("medium")).thenReturn(mediumTournaments);
        when(tournamentRepository.countParticipants(anyLong())).thenReturn(12L);
        when(tournamentRepository.getAverageScore(anyLong())).thenReturn(68.7);
        when(tournamentRepository.countLikes(anyLong())).thenReturn(7L);

        // When
        List<TournamentResponse> responses = tournamentService.getTournamentsByDifficulty("medium");

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getDifficulty()).isEqualTo("medium");
        assertThat(responses.get(0).getParticipantCount()).isEqualTo(12L);
    }

    @Test
    void existsById_TournamentExists_ReturnsTrue() {
        // Given
        when(tournamentRepository.existsById(1L)).thenReturn(true);

        // When
        boolean exists = tournamentService.existsById(1L);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_TournamentDoesNotExist_ReturnsFalse() {
        // Given
        when(tournamentRepository.existsById(999L)).thenReturn(false);

        // When
        boolean exists = tournamentService.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void getTotalTournamentCount_Success() {
        // Given
        when(tournamentRepository.count()).thenReturn(15L);

        // When
        long count = tournamentService.getTotalTournamentCount();

        // Then
        assertThat(count).isEqualTo(15L);
    }
}