package com.quiztournament.quiz_backend.repository;

import com.quiztournament.quiz_backend.entity.UserTournamentScore;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserTournamentScore entity
 * Handles quiz results, leaderboards, and user participation tracking
 */
@Repository
public interface UserTournamentScoreRepository extends JpaRepository<UserTournamentScore, Long> {

    // Find score for specific user and tournament combination
    Optional<UserTournamentScore> findByUserAndTournament(User user, Tournament tournament);

    // Check if user has already participated in a tournament
    boolean existsByUserAndTournament(User user, Tournament tournament);

    // Find all scores for a specific user (user's quiz history)
    List<UserTournamentScore> findByUserOrderByCompletedAtDesc(User user);

    // Find all scores for a specific tournament (tournament leaderboard)
    @Query("SELECT uts FROM UserTournamentScore uts JOIN FETCH uts.user WHERE uts.tournament = :tournament ORDER BY uts.score DESC")
    List<UserTournamentScore> findByTournamentOrderByScoreDesc(@Param("tournament") Tournament tournament);

    // Find top scores for a tournament (leaderboard with limit)
    @Query("SELECT uts FROM UserTournamentScore uts JOIN FETCH uts.user WHERE uts.tournament = :tournament ORDER BY uts.score DESC LIMIT :limit")
    List<UserTournamentScore> findTopScoresByTournament(@Param("tournament") Tournament tournament, @Param("limit") int limit);

    // Find all users who passed a specific tournament
    List<UserTournamentScore> findByTournamentAndPassedTrue(Tournament tournament);

    // Find all users who failed a specific tournament
    List<UserTournamentScore> findByTournamentAndPassedFalse(Tournament tournament);

    // Count total participants for a tournament
    long countByTournament(Tournament tournament);

    // Get average score for a tournament
    @Query("SELECT AVG(uts.score) FROM UserTournamentScore uts WHERE uts.tournament = :tournament")
    Double findAverageScoreByTournament(@Param("tournament") Tournament tournament);

    // Find users with scores above a certain threshold
    @Query("SELECT uts FROM UserTournamentScore uts WHERE uts.tournament = :tournament AND uts.score >= :minScore ORDER BY uts.score DESC")
    List<UserTournamentScore> findByTournamentAndScoreGreaterThanEqual(@Param("tournament") Tournament tournament, @Param("minScore") Integer minScore);

    // Get user's best scores across all tournaments
    @Query("SELECT uts FROM UserTournamentScore uts WHERE uts.user = :user ORDER BY uts.score DESC")
    List<UserTournamentScore> findUserBestScores(@Param("user") User user);
}