package com.quiztournament.quiz_backend.repository;

import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import com.quiztournament.quiz_backend.entity.UserTournamentScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests for TournamentRepository
 * Tests JPA queries and database operations
 */
@DataJpaTest
class TournamentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTournamentScoreRepository userTournamentScoreRepository;

    private User adminUser;
    private User playerUser;
    private Tournament upcomingTournament;
    private Tournament ongoingTournament;
    private Tournament pastTournament;

    @BeforeEach
    void setUp() {
        // Create test users
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("password");
        adminUser.setRole(UserRole.ADMIN);
        adminUser = entityManager.persistAndFlush(adminUser);

        playerUser = new User();
        playerUser.setUsername("player");
        playerUser.setFirstName("Player");
        playerUser.setLastName("User");
        playerUser.setEmail("player@test.com");
        playerUser.setPassword("password");
        playerUser.setRole(UserRole.PLAYER);
        playerUser = entityManager.persistAndFlush(playerUser);

        // Create tournaments with different statuses
        upcomingTournament = new Tournament();
        upcomingTournament.setName("Upcoming Tournament");
        upcomingTournament.setCategory("science");
        upcomingTournament.setDifficulty("easy");
        upcomingTournament.setStartDate(LocalDate.now().plusDays(5));
        upcomingTournament.setEndDate(LocalDate.now().plusDays(10));
        upcomingTournament.setMinPassingScore(60.0);
        upcomingTournament.setCreatedBy(adminUser);
        upcomingTournament = entityManager.persistAndFlush(upcomingTournament);

        ongoingTournament = new Tournament();
        ongoingTournament.setName("Ongoing Tournament");
        ongoingTournament.setCategory("history");
        ongoingTournament.setDifficulty("medium");
        ongoingTournament.setStartDate(LocalDate.now().minusDays(2));
        ongoingTournament.setEndDate(LocalDate.now().plusDays(3));
        ongoingTournament.setMinPassingScore(70.0);
        ongoingTournament.setCreatedBy(adminUser);
        ongoingTournament = entityManager.persistAndFlush(ongoingTournament);

        pastTournament = new Tournament();
        pastTournament.setName("Past Tournament");
        pastTournament.setCategory("sports");
        pastTournament.setDifficulty("hard");
        pastTournament.setStartDate(LocalDate.now().minusDays(10));
        pastTournament.setEndDate(LocalDate.now().minusDays(5));
        pastTournament.setMinPassingScore(80.0);
        pastTournament.setCreatedBy(adminUser);
        pastTournament = entityManager.persistAndFlush(pastTournament);

        entityManager.clear();
    }

    @Test
    void findByCreatedBy_Success() {
        // When
        List<Tournament> tournaments = tournamentRepository.findByCreatedBy(adminUser);

        // Then
        assertThat(tournaments).hasSize(3);
        assertThat(tournaments).extracting(Tournament::getName)
                .containsExactlyInAnyOrder("Upcoming Tournament", "Ongoing Tournament", "Past Tournament");
    }

    @Test
    void findByCategory_Success() {
        // When
        List<Tournament> scienceTournaments = tournamentRepository.findByCategory("science");

        // Then
        assertThat(scienceTournaments).hasSize(1);
        assertThat(scienceTournaments.get(0).getName()).isEqualTo("Upcoming Tournament");
    }

    @Test
    void findByDifficulty_Success() {
        // When
        List<Tournament> mediumTournaments = tournamentRepository.findByDifficulty("medium");

        // Then
        assertThat(mediumTournaments).hasSize(1);
        assertThat(mediumTournaments.get(0).getName()).isEqualTo("Ongoing Tournament");
    }

    @Test
    void findUpcomingTournaments_Success() {
        // When
        List<Tournament> upcoming = tournamentRepository.findUpcomingTournaments(LocalDate.now());

        // Then
        assertThat(upcoming).hasSize(1);
        assertThat(upcoming.get(0).getName()).isEqualTo("Upcoming Tournament");
    }

    @Test
    void findOngoingTournaments_Success() {
        // When
        List<Tournament> ongoing = tournamentRepository.findOngoingTournaments(LocalDate.now());

        // Then
        assertThat(ongoing).hasSize(1);
        assertThat(ongoing.get(0).getName()).isEqualTo("Ongoing Tournament");
    }

    @Test
    void findPastTournaments_Success() {
        // When
        List<Tournament> past = tournamentRepository.findPastTournaments(LocalDate.now());

        // Then
        assertThat(past).hasSize(1);
        assertThat(past.get(0).getName()).isEqualTo("Past Tournament");
    }

    @Test
    void findTournamentsParticipatedByUser_Success() {
        // Given - Create a participation record
        UserTournamentScore score = new UserTournamentScore();
        score.setUser(playerUser);
        score.setTournament(ongoingTournament);
        score.setScore(8);
        score.setCompletedAt(LocalDateTime.now());
        score.setPassed(true);
        entityManager.persistAndFlush(score);

        // When
        List<Tournament> participatedTournaments =
                tournamentRepository.findTournamentsParticipatedByUser(playerUser.getId());

        // Then
        assertThat(participatedTournaments).hasSize(1);
        assertThat(participatedTournaments.get(0).getName()).isEqualTo("Ongoing Tournament");
    }

    @Test
    void findTournamentsNotParticipatedByUser_Success() {
        // Given - Create a participation record for one tournament
        UserTournamentScore score = new UserTournamentScore();
        score.setUser(playerUser);
        score.setTournament(ongoingTournament);
        score.setScore(8);
        score.setCompletedAt(LocalDateTime.now());
        score.setPassed(true);
        entityManager.persistAndFlush(score);

        // When
        List<Tournament> notParticipatedTournaments =
                tournamentRepository.findTournamentsNotParticipatedByUser(playerUser.getId());

        // Then
        assertThat(notParticipatedTournaments).hasSize(2);
        assertThat(notParticipatedTournaments).extracting(Tournament::getName)
                .containsExactlyInAnyOrder("Upcoming Tournament", "Past Tournament");
    }

    @Test
    void countParticipants_WithParticipants() {
        // Given - Create multiple participation records
        UserTournamentScore score1 = new UserTournamentScore();
        score1.setUser(playerUser);
        score1.setTournament(ongoingTournament);
        score1.setScore(8);
        score1.setCompletedAt(LocalDateTime.now());
        score1.setPassed(true);
        entityManager.persistAndFlush(score1);

        // Create another user and score
        User anotherPlayer = new User();
        anotherPlayer.setUsername("player2");
        anotherPlayer.setFirstName("Player2");
        anotherPlayer.setLastName("User");
        anotherPlayer.setEmail("player2@test.com");
        anotherPlayer.setPassword("password");
        anotherPlayer.setRole(UserRole.PLAYER);
        anotherPlayer = entityManager.persistAndFlush(anotherPlayer);

        UserTournamentScore score2 = new UserTournamentScore();
        score2.setUser(anotherPlayer);
        score2.setTournament(ongoingTournament);
        score2.setScore(6);
        score2.setCompletedAt(LocalDateTime.now());
        score2.setPassed(false);
        entityManager.persistAndFlush(score2);

        // When
        Long participantCount = tournamentRepository.countParticipants(ongoingTournament.getId());

        // Then
        assertThat(participantCount).isEqualTo(2L);
    }

    @Test
    void countParticipants_NoParticipants() {
        // When
        Long participantCount = tournamentRepository.countParticipants(upcomingTournament.getId());

        // Then
        assertThat(participantCount).isEqualTo(0L);
    }

    @Test
    void getAverageScore_WithScores() {
        // Given - Create multiple participation records
        UserTournamentScore score1 = new UserTournamentScore();
        score1.setUser(playerUser);
        score1.setTournament(ongoingTournament);
        score1.setScore(8);
        score1.setCompletedAt(LocalDateTime.now());
        score1.setPassed(true);
        entityManager.persistAndFlush(score1);

        User anotherPlayer = new User();
        anotherPlayer.setUsername("player2");
        anotherPlayer.setFirstName("Player2");
        anotherPlayer.setLastName("User");
        anotherPlayer.setEmail("player2@test.com");
        anotherPlayer.setPassword("password");
        anotherPlayer.setRole(UserRole.PLAYER);
        anotherPlayer = entityManager.persistAndFlush(anotherPlayer);

        UserTournamentScore score2 = new UserTournamentScore();
        score2.setUser(anotherPlayer);
        score2.setTournament(ongoingTournament);
        score2.setScore(6);
        score2.setCompletedAt(LocalDateTime.now());
        score2.setPassed(false);
        entityManager.persistAndFlush(score2);

        // When
        Double averageScore = tournamentRepository.getAverageScore(ongoingTournament.getId());

        // Then
        assertThat(averageScore).isEqualTo(7.0);
    }

    @Test
    void getAverageScore_NoScores() {
        // When
        Double averageScore = tournamentRepository.getAverageScore(upcomingTournament.getId());

        // Then
        assertThat(averageScore).isNull();
    }

    @Test
    void findAll_OrderedByCreatedAt() {
        // When
        List<Tournament> tournaments = tournamentRepository.findAll();

        // Then
        assertThat(tournaments).hasSize(3);
        // Verify tournaments exist (order may vary based on creation time)
        assertThat(tournaments).extracting(Tournament::getName)
                .containsExactlyInAnyOrder("Upcoming Tournament", "Ongoing Tournament", "Past Tournament");
    }

    @Test
    void existsById_TournamentExists() {
        // When
        boolean exists = tournamentRepository.existsById(ongoingTournament.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_TournamentDoesNotExist() {
        // When
        boolean exists = tournamentRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteById_Success() {
        // Given
        Long tournamentId = upcomingTournament.getId();
        assertThat(tournamentRepository.existsById(tournamentId)).isTrue();

        // When
        tournamentRepository.deleteById(tournamentId);
        entityManager.flush();

        // Then
        assertThat(tournamentRepository.existsById(tournamentId)).isFalse();
    }

    @Test
    void count_Success() {
        // When
        long count = tournamentRepository.count();

        // Then
        assertThat(count).isEqualTo(3L);
    }
}