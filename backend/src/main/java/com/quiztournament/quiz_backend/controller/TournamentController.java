package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import com.quiztournament.quiz_backend.dto.TournamentUpdateRequest;
import com.quiztournament.quiz_backend.dto.TournamentResponse;
import com.quiztournament.quiz_backend.entity.QuizAnswer;
import com.quiztournament.quiz_backend.entity.QuizResult;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.TournamentStatus;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.repository.QuizAnswerRepository;
import com.quiztournament.quiz_backend.repository.QuizResultRepository;
import com.quiztournament.quiz_backend.repository.TournamentRepository;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Tournament operations
 * Handles tournament CRUD operations and statistics
 */
@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "http://localhost:3000")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new tournament (Admin only)
     * POST /api/tournaments
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTournament(@Valid @RequestBody TournamentCreateRequest request) {
        try {
            TournamentResponse response = tournamentService.createTournament(request);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Tournament created successfully");
            responseBody.put("tournament", response);
            responseBody.put("success", true);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all tournaments (Admin only)
     * GET /api/tournaments
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllTournaments() {
        try {
            List<TournamentResponse> tournaments = tournamentService.getAllTournaments();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("tournaments", tournaments);
            responseBody.put("count", tournaments.size());
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournament by ID (Authenticated users)
     * GET /api/tournaments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTournamentById(@PathVariable Long id) {
        try {
            TournamentResponse tournament = tournamentService.getTournamentById(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("tournament", tournament);
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Update tournament (Admin only)
     * PUT /api/tournaments/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTournament(@PathVariable Long id,
                                              @Valid @RequestBody TournamentUpdateRequest request) {
        try {
            TournamentResponse response = tournamentService.updateTournament(id, request);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Tournament updated successfully");
            responseBody.put("tournament", response);
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Delete tournament (Admin only)
     * DELETE /api/tournaments/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTournament(@PathVariable Long id) {
        try {
            tournamentService.deleteTournament(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Tournament deleted successfully");
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournaments by status (Authenticated users)
     * GET /api/tournaments/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getTournamentsByStatus(@PathVariable String status) {
        try {
            TournamentStatus tournamentStatus = TournamentStatus.valueOf(status.toUpperCase());
            List<TournamentResponse> tournaments = tournamentService.getTournamentsByStatus(tournamentStatus);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("tournaments", tournaments);
            responseBody.put("status", status.toLowerCase());
            responseBody.put("count", tournaments.size());
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid status. Valid statuses are: upcoming, ongoing, past");
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournaments created by current admin (Admin only)
     * GET /api/tournaments/my-tournaments
     */
    @GetMapping("/my-tournaments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMyTournaments() {
        try {
            List<TournamentResponse> tournaments = tournamentService.getMyTournaments();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("tournaments", tournaments);
            responseBody.put("count", tournaments.size());
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournaments by category (Authenticated users)
     * GET /api/tournaments/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getTournamentsByCategory(@PathVariable String category) {
        try {
            List<TournamentResponse> tournaments = tournamentService.getTournamentsByCategory(category);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("tournaments", tournaments);
            responseBody.put("category", category);
            responseBody.put("count", tournaments.size());
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournaments by difficulty (Authenticated users)
     * GET /api/tournaments/difficulty/{difficulty}
     */
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<?> getTournamentsByDifficulty(@PathVariable String difficulty) {
        try {
            List<TournamentResponse> tournaments = tournamentService.getTournamentsByDifficulty(difficulty);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("tournaments", tournaments);
            responseBody.put("difficulty", difficulty);
            responseBody.put("count", tournaments.size());
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournament statistics (Admin only)
     * GET /api/tournaments/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentStatistics() {
        try {
            long totalTournaments = tournamentService.getTotalTournamentCount();
            List<TournamentResponse> upcomingTournaments = tournamentService.getTournamentsByStatus(TournamentStatus.UPCOMING);
            List<TournamentResponse> ongoingTournaments = tournamentService.getTournamentsByStatus(TournamentStatus.ONGOING);
            List<TournamentResponse> pastTournaments = tournamentService.getTournamentsByStatus(TournamentStatus.PAST);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("totalTournaments", totalTournaments);
            responseBody.put("upcomingCount", upcomingTournaments.size());
            responseBody.put("ongoingCount", ongoingTournaments.size());
            responseBody.put("pastCount", pastTournaments.size());
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     * GET /api/tournaments/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tournament service is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Get quiz results for a tournament (Admin only)
     * GET /api/tournaments/{id}/results
     */
    @GetMapping("/{id}/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentResults(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));

            List<QuizResult> results = quizResultRepository.findByTournamentOrderByPercentageDescCompletedAtAsc(tournament);
            
            // Calculate statistics
            long totalParticipants = results.size();
            long passedCount = results.stream().mapToLong(r -> r.getPassed() ? 1 : 0).sum();
            double averageScore = results.stream().mapToDouble(QuizResult::getPercentage).average().orElse(0.0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("tournament", tournament);
            response.put("results", results);
            response.put("statistics", Map.of(
                "totalParticipants", totalParticipants,
                "passedCount", passedCount,
                "failedCount", totalParticipants - passedCount,
                "passRate", totalParticipants > 0 ? (passedCount * 100.0 / totalParticipants) : 0.0,
                "averageScore", Math.round(averageScore * 100.0) / 100.0
            ));
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Debug endpoint to check quiz data (Admin only)
     * GET /api/tournaments/{id}/debug
     */
    @GetMapping("/{id}/debug")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> debugTournamentData(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));

            List<QuizResult> quizResults = quizResultRepository.findByTournamentOrderByPercentageDescCompletedAtAsc(tournament);

            Map<String, Object> response = new HashMap<>();
            response.put("tournament", Map.of(
                "id", tournament.getId(),
                "name", tournament.getName(),
                "status", tournament.getStatus()
            ));
            response.put("quizResultsCount", quizResults.size());
            response.put("quizResults", quizResults.stream().map(qr -> Map.of(
                "id", qr.getId(),
                "user", qr.getUser().getFirstName() + " " + qr.getUser().getLastName(),
                "email", qr.getUser().getEmail(),
                "score", qr.getScore(),
                "totalQuestions", qr.getTotalQuestions(),
                "percentage", qr.getPercentage(),
                "passed", qr.getPassed(),
                "completedAt", qr.getCompletedAt()
            )).toList());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get detailed answers for tournament review (Admin only)
     * GET /api/tournaments/{id}/detailed-answers
     */
    @GetMapping("/{id}/detailed-answers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentDetailedAnswers(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));

            List<QuizAnswer> detailedAnswers = quizAnswerRepository.findByTournamentOrderByUserAndQuestion(tournament);
            
            // Group answers by user for better organization
            Map<String, List<Map<String, Object>>> answersByUser = new HashMap<>();
            
            for (QuizAnswer answer : detailedAnswers) {
                String username = answer.getQuizResult().getUser().getUsername();
                String userDisplayName = answer.getQuizResult().getUser().getFirstName() + " " + 
                                       answer.getQuizResult().getUser().getLastName();
                
                Map<String, Object> answerData = new HashMap<>();
                answerData.put("questionNumber", answer.getQuestionNumber());
                answerData.put("questionText", answer.getQuestionText());
                answerData.put("userAnswer", answer.getUserAnswer());
                answerData.put("correctAnswer", answer.getCorrectAnswer());
                answerData.put("isCorrect", answer.getIsCorrect());
                answerData.put("answeredAt", answer.getAnsweredAt());
                
                String userKey = username + " (" + userDisplayName + ")";
                answersByUser.computeIfAbsent(userKey, k -> new ArrayList<>()).add(answerData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tournament", Map.of(
                "id", tournament.getId(),
                "name", tournament.getName(),
                "category", tournament.getCategory(),
                "difficulty", tournament.getDifficulty()
            ));
            response.put("answersByUser", answersByUser);
            response.put("totalUsers", answersByUser.size());
            response.put("totalAnswers", detailedAnswers.size());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get user's own detailed answers for a tournament (Authenticated users)
     * GET /api/tournaments/{id}/my-answers
     */
    @GetMapping("/{id}/my-answers")
    public ResponseEntity<?> getMyTournamentAnswers(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));

            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("User not authenticated");
            }

            com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
            
            Long userId = userPrincipal.getId();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get user's answers for this tournament
            List<QuizAnswer> myAnswers = quizAnswerRepository.findByTournamentAndUserOrderByQuestion(tournament, userId);
            
            if (myAnswers.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "No quiz answers found. You may not have completed this tournament yet.");
                errorResponse.put("success", false);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Format answers for frontend
            List<Map<String, Object>> formattedAnswers = new ArrayList<>();
            
            for (QuizAnswer answer : myAnswers) {
                Map<String, Object> answerData = new HashMap<>();
                answerData.put("questionNumber", answer.getQuestionNumber());
                answerData.put("questionText", answer.getQuestionText());
                answerData.put("userAnswer", answer.getUserAnswer());
                answerData.put("correctAnswer", answer.getCorrectAnswer());
                answerData.put("isCorrect", answer.getIsCorrect());
                answerData.put("answeredAt", answer.getAnsweredAt());
                formattedAnswers.add(answerData);
            }

            // Get quiz result for summary
            Optional<QuizResult> quizResultOpt = quizResultRepository.findByUserAndTournament(user, tournament);
            
            Map<String, Object> response = new HashMap<>();
            response.put("tournament", Map.of(
                "id", tournament.getId(),
                "name", tournament.getName(),
                "category", tournament.getCategory(),
                "difficulty", tournament.getDifficulty()
            ));
            response.put("answers", formattedAnswers);
            response.put("totalQuestions", formattedAnswers.size());
            response.put("correctAnswers", formattedAnswers.stream().mapToInt(a -> (Boolean) a.get("isCorrect") ? 1 : 0).sum());
            
            if (quizResultOpt.isPresent()) {
                QuizResult result = quizResultOpt.get();
                response.put("quizResult", Map.of(
                    "score", result.getScore(),
                    "totalQuestions", result.getTotalQuestions(),
                    "percentage", result.getPercentage(),
                    "passed", result.getPassed(),
                    "completedAt", result.getCompletedAt()
                ));
            }
            
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get enhanced tournament scores with statistics (Assessment requirement)
     * GET /api/tournaments/{id}/enhanced-scores
     */
    @GetMapping("/{id}/enhanced-scores")
    public ResponseEntity<?> getEnhancedTournamentScores(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tournament not found"));

            // Get all quiz results for this tournament, sorted by score descending
            List<QuizResult> results = quizResultRepository.findByTournamentOrderByPercentageDescCompletedAtAsc(tournament);
            
            // Calculate statistics
            long totalPlayers = results.size();
            double averageScore = results.stream().mapToDouble(QuizResult::getPercentage).average().orElse(0.0);
            
            // Get tournament likes count (placeholder for now)
            long likesCount = (long) (Math.random() * 50); // Simulated likes for demo

            // Format results for frontend
            List<Map<String, Object>> formattedResults = new ArrayList<>();
            
            for (int i = 0; i < results.size(); i++) {
                QuizResult result = results.get(i);
                Map<String, Object> resultData = new HashMap<>();
                
                resultData.put("rank", i + 1);
                resultData.put("playerName", result.getUser().getFirstName() + " " + result.getUser().getLastName());
                resultData.put("username", result.getUser().getUsername());
                resultData.put("score", result.getScore());
                resultData.put("totalQuestions", result.getTotalQuestions());
                resultData.put("percentage", Math.round(result.getPercentage() * 100.0) / 100.0);
                resultData.put("passed", result.getPassed());
                resultData.put("completedDate", result.getCompletedAt());
                resultData.put("timeTaken", result.getTimeTakenSeconds());
                
                formattedResults.add(resultData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tournament", Map.of(
                "id", tournament.getId(),
                "name", tournament.getName(),
                "category", tournament.getCategory(),
                "difficulty", tournament.getDifficulty()
            ));
            response.put("statistics", Map.of(
                "totalPlayers", totalPlayers,
                "averageScore", Math.round(averageScore * 100.0) / 100.0,
                "likesCount", likesCount,
                "passRate", totalPlayers > 0 ? Math.round((results.stream().mapToLong(r -> r.getPassed() ? 1 : 0).sum() * 100.0 / totalPlayers) * 100.0) / 100.0 : 0.0
            ));
            response.put("scores", formattedResults);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}