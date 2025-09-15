package com.quiztournament.quiz_backend.repository;

import com.quiztournament.quiz_backend.entity.QuizAnswer;
import com.quiztournament.quiz_backend.entity.QuizResult;
import com.quiztournament.quiz_backend.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for QuizAnswer entity
 */
@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    
    /**
     * Find all answers for a specific quiz result
     */
    List<QuizAnswer> findByQuizResultOrderByQuestionNumber(QuizResult quizResult);
    
    /**
     * Find all answers for a tournament (admin review)
     */
    @Query("SELECT qa FROM QuizAnswer qa " +
           "JOIN qa.quizResult qr " +
           "WHERE qr.tournament = :tournament " +
           "ORDER BY qr.user.username, qa.questionNumber")
    List<QuizAnswer> findByTournamentOrderByUserAndQuestion(@Param("tournament") Tournament tournament);
    
    /**
     * Find answers for a specific tournament and user
     */
    @Query("SELECT qa FROM QuizAnswer qa " +
           "JOIN qa.quizResult qr " +
           "WHERE qr.tournament = :tournament AND qr.user.id = :userId " +
           "ORDER BY qa.questionNumber")
    List<QuizAnswer> findByTournamentAndUserOrderByQuestion(@Param("tournament") Tournament tournament, 
                                                           @Param("userId") Long userId);
    
    /**
     * Count correct answers for a tournament
     */
    @Query("SELECT COUNT(qa) FROM QuizAnswer qa " +
           "JOIN qa.quizResult qr " +
           "WHERE qr.tournament = :tournament AND qa.isCorrect = true")
    Long countCorrectAnswersByTournament(@Param("tournament") Tournament tournament);
    
    /**
     * Count total answers for a tournament
     */
    @Query("SELECT COUNT(qa) FROM QuizAnswer qa " +
           "JOIN qa.quizResult qr " +
           "WHERE qr.tournament = :tournament")
    Long countTotalAnswersByTournament(@Param("tournament") Tournament tournament);
}