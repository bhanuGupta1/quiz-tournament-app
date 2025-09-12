package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.dto.QuizAnswerRequest;
import com.quiztournament.quiz_backend.dto.QuizResultResponse;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.service.QuizParticipationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Quiz Participation
 * Handles quiz participation, score submission, and results retrieval
 */
@RestController
@RequestMapping("/api/participation")
@CrossOrigin(origins = "http://localhost:3000")
public class QuizParticipationController {

    @Autowired
    private QuizParticipationService quizParticipationService;

    /**
     * Start quiz participation (Player only)
     * POST /api/participation/tournaments/{id}/start
     */
    @PostMapping("/tournaments/{id}/start")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> startQuizParticipation(@PathVariable Long id) {
        try {
            Map<String, Object> result = quizParticipationService.startQuizParticipation(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.putAll(result);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Submit complete quiz answers (Player only)
     * POST /api/participation/tournaments/{id}/submit
     */
    @PostMapping("/tournaments/{id}/submit")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> submitQuizAnswers(@PathVariable Long id,
                                               @Valid @RequestBody List<QuizAnswerRequest> answers) {
        try {
            QuizResultResponse result = quizParticipationService.submitQuizAnswers(id, answers);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("result", result);
            responseBody.put("message", result.getPerformanceMessage());
            responseBody.put("grade", result.getGrade());

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Check participation eligibility (Player only)
     * GET /api/participation/tournaments/{id}/eligibility
     */
    @GetMapping("/tournaments/{id}/eligibility")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> checkParticipationEligibility(@PathVariable Long id) {
        try {
            Map<String, Object> eligibility = quizParticipationService.checkParticipationEligibility(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.putAll(eligibility);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get user's quiz history (Player only)
     * GET /api/participation/my-quiz-history
     */
    @GetMapping("/my-quiz-history")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getUserQuizHistory() {
        try {
            List<QuizResultResponse> history = quizParticipationService.getUserQuizHistory();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("quizHistory", history);
            responseBody.put("totalParticipated", history.size());

            // Calculate user statistics
            if (!history.isEmpty()) {
                long passedCount = history.stream().mapToLong(quiz -> quiz.getPassed() ? 1 : 0).sum();
                double averageScore = history.stream().mapToDouble(QuizResultResponse::getPercentage).average().orElse(0.0);

                responseBody.put("passedCount", passedCount);
                responseBody.put("failedCount", history.size() - passedCount);
                responseBody.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
                responseBody.put("passRate", (passedCount * 100.0) / history.size());
            }

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournaments user has participated in (Player only)
     * GET /api/participation/my-tournaments
     */
    @GetMapping("/my-tournaments")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getUserParticipatedTournaments() {
        try {
            List<Tournament> tournaments = quizParticipationService.getUserParticipatedTournaments();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("tournaments", tournaments);
            responseBody.put("count", tournaments.size());
            responseBody.put("message", "Tournaments you have completed");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get available tournaments for participation (Player only)
     * GET /api/participation/available-tournaments
     */
    @GetMapping("/available-tournaments")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getUserAvailableTournaments() {
        try {
            List<Tournament> tournaments = quizParticipationService.getUserAvailableTournaments();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("tournaments", tournaments);
            responseBody.put("count", tournaments.size());
            responseBody.put("message", "Tournaments available for participation");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get user's result for specific tournament (Player only)
     * GET /api/participation/tournaments/{id}/my-result
     */
    @GetMapping("/tournaments/{id}/my-result")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getUserTournamentResult(@PathVariable Long id) {
        try {
            QuizResultResponse result = quizParticipationService.getUserTournamentResult(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("result", result);
            responseBody.put("message", result.getPerformanceMessage());
            responseBody.put("grade", result.getGrade());

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournament leaderboard (Authenticated users)
     * GET /api/participation/tournaments/{id}/leaderboard
     */
    @GetMapping("/tournaments/{id}/leaderboard")
    public ResponseEntity<?> getTournamentLeaderboard(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "10") int limit) {
        try {
            List<QuizResultResponse> leaderboard;

            if (limit > 0) {
                leaderboard = quizParticipationService.getTopScores(id, Math.min(limit, 50)); // Max 50 results
            } else {
                leaderboard = quizParticipationService.getTournamentLeaderboard(id);
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("leaderboard", leaderboard);
            responseBody.put("totalEntries", leaderboard.size());
            responseBody.put("tournamentId", id);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            
            // Debug logging for leaderboard issues
            System.err.println("Leaderboard error for tournament " + id + ": " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournament statistics (Admin and Players can view)
     * GET /api/participation/tournaments/{id}/statistics
     */
    @GetMapping("/tournaments/{id}/statistics")
    public ResponseEntity<?> getTournamentStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> statistics = quizParticipationService.getTournamentStatistics(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("statistics", statistics);
            responseBody.put("tournamentId", id);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check for participation service
     * GET /api/participation/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Quiz Participation service is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}