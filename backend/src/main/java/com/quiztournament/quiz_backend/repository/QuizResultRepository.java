package com.quiztournament.quiz_backend.repository;

import com.quiztournament.quiz_backend.entity.QuizResult;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    
    /**
     * Find quiz result by user and tournament
     */
    Optional<QuizResult> findByUserAndTournament(User user, Tournament tournament);
    
    /**
     * Find all quiz results for a tournament
     */
    List<QuizResult> findByTournamentOrderByPercentageDescCompletedAtAsc(Tournament tournament);
    
    /**
     * Find all quiz results for a user
     */
    List<QuizResult> findByUserOrderByCompletedAtDesc(User user);
    
    /**
     * Get tournament leaderboard (top performers)
     */
    @Query("SELECT qr FROM QuizResult qr WHERE qr.tournament = :tournament ORDER BY qr.percentage DESC, qr.completedAt ASC")
    List<QuizResult> findTournamentLeaderboard(@Param("tournament") Tournament tournament);
    
    /**
     * Count total participants for a tournament
     */
    long countByTournament(Tournament tournament);
    
    /**
     * Count passed participants for a tournament
     */
    long countByTournamentAndPassedTrue(Tournament tournament);
}