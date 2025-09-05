package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.dto.QuizAnswerRequest;
import com.quiztournament.quiz_backend.dto.QuizResultResponse;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserTournamentScore;
import com.quiztournament.quiz_backend.repository.TournamentRepository;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.repository.UserTournamentScoreRepository;
import com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for handling quiz participation and score persistence
 * Manages the complete quiz flow from start to finish with database integration
 */
@Service
@Transactional
public class QuizParticipationService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTournamentScoreRepository userTournamentScoreRepository;

    @Autowired
    private QuestionService questionService;

    /**
     * Start quiz participation for a tournament
     * @param tournamentId Tournament ID
     * @return Initial quiz session information
     */
    public Map<String, Object> startQuizParticipation(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        // Check if user has already participated
        if (userTournamentScoreRepository.existsByUserAndTournament(currentUser, tournament)) {
            throw new RuntimeException("You have already participated in this tournament");
        }

        // Validate tournament status
        switch (tournament.getStatus()) {
            case UPCOMING:
                throw new RuntimeException("Tournament has not started yet");
            case PAST:
                throw new RuntimeException("Tournament has already ended");
            case ONGOING:
                // Proceed with quiz start
                break;
        }

        // Start the quiz session (this will initialize questions)
        questionService.getTournamentQuestions(tournamentId);

        Map<String, Object> response = new HashMap<>();
        response.put("tournamentId", tournamentId);
        response.put("tournamentName", tournament.getName());
        response.put("category", tournament.getCategory());
        response.put("difficulty", tournament.getDifficulty());
        response.put("totalQuestions", 10);
        response.put("minPassingScore", tournament.getMinPassingScore());
        response.put("message", "Quiz started successfully! Good luck!");
        response.put("startedAt", LocalDateTime.now());

        return response;
    }

    /**
     * Submit quiz answers and calculate final score
     * @param tournamentId Tournament ID
     * @param answers List of answers submitted by user
     * @return Final quiz result with score persistence
     */
    public QuizResultResponse submitQuizAnswers(Long tournamentId, List<QuizAnswerRequest> answers) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        // Check if user has already participated
        if (userTournamentScoreRepository.existsByUserAndTournament(currentUser, tournament)) {
            throw new RuntimeException("You have already participated in this tournament");
        }

        // Validate we have exactly 10 answers
        if (answers.size() != 10) {
            throw new RuntimeException("Quiz must have exactly 10 answers. Received: " + answers.size());
        }

        // Validate and calculate score
        Map<Integer, String> userAnswers = new HashMap<>();
        Map<Integer, String> correctAnswers = new HashMap<>();
        Map<Integer, Boolean> answerResults = new HashMap<>();
        int correctCount = 0;

        for (QuizAnswerRequest answer : answers) {
            if (answer.getQuestionNumber() < 1 || answer.getQuestionNumber() > 10) {
                throw new RuntimeException("Invalid question number: " + answer.getQuestionNumber());
            }

            // Validate answer with QuestionService
            QuestionService.AnswerValidationResult result = questionService.validateAnswer(
                    tournamentId, answer.getQuestionNumber(), answer.getAnswer()
            );

            userAnswers.put(answer.getQuestionNumber(), answer.getAnswer());
            correctAnswers.put(answer.getQuestionNumber(), result.getCorrectAnswer());
            answerResults.put(answer.getQuestionNumber(), result.isCorrect());

            if (result.isCorrect()) {
                correctCount++;
            }
        }

        // Calculate percentage and pass/fail
        double percentage = (correctCount / 10.0) * 100.0;
        boolean passed = percentage >= tournament.getMinPassingScore();

        // Save score to database
        UserTournamentScore userScore = new UserTournamentScore(currentUser, tournament, correctCount);
        userScore.setPassed(passed);
        userScore.setCompletedAt(LocalDateTime.now());

        UserTournamentScore savedScore = userTournamentScoreRepository.save(userScore);

        // Clean up quiz session
        questionService.clearTournamentCache(tournamentId);

        // Return detailed result
        return new QuizResultResponse(savedScore, userAnswers, correctAnswers, answerResults);
    }

    /**
     * Get user's quiz history
     * @return List of completed quizzes for current user
     */
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getUserQuizHistory() {
        User currentUser = getCurrentUser();
        List<UserTournamentScore> scores = userTournamentScoreRepository.findByUserOrderByCompletedAtDesc(currentUser);

        return scores.stream()
                .map(QuizResultResponse::fromUserTournamentScore)
                .collect(Collectors.toList());
    }

    /**
     * Get tournament leaderboard
     * @param tournamentId Tournament ID
     * @return Ordered list of top scores
     */
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getTournamentLeaderboard(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        List<UserTournamentScore> scores = userTournamentScoreRepository.findByTournamentOrderByScoreDesc(tournament);

        return scores.stream()
                .map(QuizResultResponse::fromUserTournamentScore)
                .collect(Collectors.toList());
    }

    /**
     * Get tournament leaderboard with limit
     * @param tournamentId Tournament ID
     * @param limit Maximum number of results
     * @return Top scores limited to specified count
     */
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getTopScores(Long tournamentId, int limit) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        List<UserTournamentScore> scores = userTournamentScoreRepository.findTopScoresByTournament(tournament, limit);

        return scores.stream()
                .map(QuizResultResponse::fromUserTournamentScore)
                .collect(Collectors.toList());
    }

    /**
     * Get tournaments user has participated in
     * @return List of tournaments current user has completed
     */
    @Transactional(readOnly = true)
    public List<Tournament> getUserParticipatedTournaments() {
        User currentUser = getCurrentUser();
        return tournamentRepository.findTournamentsParticipatedByUser(currentUser.getId());
    }

    /**
     * Get tournaments user has NOT participated in yet
     * @return List of available tournaments for participation
     */
    @Transactional(readOnly = true)
    public List<Tournament> getUserAvailableTournaments() {
        User currentUser = getCurrentUser();
        return tournamentRepository.findTournamentsNotParticipatedByUser(currentUser.getId());
    }

    /**
     * Check if user can participate in tournament
     * @param tournamentId Tournament ID
     * @return Map with participation status and message
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkParticipationEligibility(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();
        Map<String, Object> result = new HashMap<>();

        // Check if already participated
        if (userTournamentScoreRepository.existsByUserAndTournament(currentUser, tournament)) {
            result.put("canParticipate", false);
            result.put("reason", "You have already participated in this tournament");
            result.put("status", "ALREADY_PARTICIPATED");
            return result;
        }

        // Check tournament status
        switch (tournament.getStatus()) {
            case UPCOMING:
                result.put("canParticipate", false);
                result.put("reason", "Tournament has not started yet");
                result.put("status", "UPCOMING");
                result.put("startDate", tournament.getStartDate());
                break;
            case PAST:
                result.put("canParticipate", false);
                result.put("reason", "Tournament has already ended");
                result.put("status", "PAST");
                result.put("endDate", tournament.getEndDate());
                break;
            case ONGOING:
                result.put("canParticipate", true);
                result.put("reason", "You can participate in this tournament");
                result.put("status", "ONGOING");
                break;
        }

        result.put("tournamentName", tournament.getName());
        result.put("category", tournament.getCategory());
        result.put("difficulty", tournament.getDifficulty());
        result.put("minPassingScore", tournament.getMinPassingScore());

        return result;
    }

    /**
     * Get detailed quiz result by ID
     * @param tournamentId Tournament ID
     * @return Detailed quiz result if user participated
     */
    @Transactional(readOnly = true)
    public QuizResultResponse getUserTournamentResult(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        UserTournamentScore score = userTournamentScoreRepository.findByUserAndTournament(currentUser, tournament)
                .orElseThrow(() -> new RuntimeException("You have not participated in this tournament yet"));

        return QuizResultResponse.fromUserTournamentScore(score);
    }

    /**
     * Get tournament statistics for participants
     * @param tournamentId Tournament ID
     * @return Statistics about tournament participation
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTournamentStatistics(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        Map<String, Object> stats = new HashMap<>();

        // Basic statistics
        Long totalParticipants = userTournamentScoreRepository.countByTournament(tournament);
        Double averageScore = userTournamentScoreRepository.findAverageScoreByTournament(tournament);
        List<UserTournamentScore> passedUsers = userTournamentScoreRepository.findByTournamentAndPassedTrue(tournament);
        List<UserTournamentScore> failedUsers = userTournamentScoreRepository.findByTournamentAndPassedFalse(tournament);

        stats.put("totalParticipants", totalParticipants);
        stats.put("averageScore", averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0);
        stats.put("passedCount", passedUsers.size());
        stats.put("failedCount", failedUsers.size());
        stats.put("passRate", totalParticipants > 0 ? (passedUsers.size() * 100.0) / totalParticipants : 0.0);

        // Additional statistics
        if (totalParticipants > 0) {
            List<UserTournamentScore> allScores = userTournamentScoreRepository.findByTournamentOrderByScoreDesc(tournament);

            stats.put("highestScore", allScores.get(0).getScore());
            stats.put("lowestScore", allScores.get(allScores.size() - 1).getScore());

            // Score distribution
            Map<String, Integer> scoreDistribution = new HashMap<>();
            scoreDistribution.put("0-3", 0);
            scoreDistribution.put("4-6", 0);
            scoreDistribution.put("7-8", 0);
            scoreDistribution.put("9-10", 0);

            for (UserTournamentScore score : allScores) {
                int s = score.getScore();
                if (s <= 3) scoreDistribution.put("0-3", scoreDistribution.get("0-3") + 1);
                else if (s <= 6) scoreDistribution.put("4-6", scoreDistribution.get("4-6") + 1);
                else if (s <= 8) scoreDistribution.put("7-8", scoreDistribution.get("7-8") + 1);
                else scoreDistribution.put("9-10", scoreDistribution.get("9-10") + 1);
            }

            stats.put("scoreDistribution", scoreDistribution);
        }

        return stats;
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();

        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }
}
