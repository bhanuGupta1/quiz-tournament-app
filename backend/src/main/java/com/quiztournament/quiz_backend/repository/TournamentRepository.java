package com.quiztournament.quiz_backend.repository;

import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Tournament entity
 * Provides database operations and custom queries for tournament management
 */
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    // Find tournaments created by a specific admin
    List<Tournament> findByCreatedBy(User createdBy);

    // Find tournaments by category
    List<Tournament> findByCategory(String category);

    // Find tournaments by difficulty
    List<Tournament> findByDifficulty(String difficulty);

    // Find upcoming tournaments (start date is in the future)
    @Query("SELECT t FROM Tournament t WHERE :currentDate < t.startDate ORDER BY t.startDate ASC")
    List<Tournament> findUpcomingTournaments(@Param("currentDate") LocalDate currentDate);

    // Find ongoing tournaments (current date is between start and end dates)
    @Query("SELECT t FROM Tournament t WHERE :currentDate >= t.startDate AND :currentDate <= t.endDate ORDER BY t.startDate ASC")
    List<Tournament> findOngoingTournaments(@Param("currentDate") LocalDate currentDate);

    // Find past tournaments (end date is in the past)
    @Query("SELECT t FROM Tournament t WHERE :currentDate > t.endDate ORDER BY t.endDate DESC")
    List<Tournament> findPastTournaments(@Param("currentDate") LocalDate currentDate);

    // Find tournaments that a specific user has participated in
    // Fixed: Use subquery to avoid DISTINCT + ORDER BY issue
    @Query("SELECT t FROM Tournament t WHERE t.id IN " +
            "(SELECT uts.tournament.id FROM UserTournamentScore uts WHERE uts.user.id = :userId) " +
            "ORDER BY t.createdAt DESC")
    List<Tournament> findTournamentsParticipatedByUser(@Param("userId") Long userId);

    // Find tournaments that a user has NOT participated in yet
    @Query("SELECT t FROM Tournament t WHERE t.id NOT IN " +
            "(SELECT uts.tournament.id FROM UserTournamentScore uts WHERE uts.user.id = :userId)")
    List<Tournament> findTournamentsNotParticipatedByUser(@Param("userId") Long userId);

    // Count total participants for a tournament
    @Query("SELECT COUNT(uts) FROM UserTournamentScore uts WHERE uts.tournament.id = :tournamentId")
    Long countParticipants(@Param("tournamentId") Long tournamentId);

    // Get average score for a tournament
    @Query("SELECT AVG(uts.score) FROM UserTournamentScore uts WHERE uts.tournament.id = :tournamentId")
    Double getAverageScore(@Param("tournamentId") Long tournamentId);

    // Count likes for a tournament
    @Query("SELECT COUNT(tl) FROM TournamentLike tl WHERE tl.tournament.id = :tournamentId")
    Long countLikes(@Param("tournamentId") Long tournamentId);
}