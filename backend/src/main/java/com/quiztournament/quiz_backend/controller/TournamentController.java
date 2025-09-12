package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import com.quiztournament.quiz_backend.dto.TournamentUpdateRequest;
import com.quiztournament.quiz_backend.dto.TournamentResponse;
import com.quiztournament.quiz_backend.entity.QuizResult;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.TournamentStatus;
import com.quiztournament.quiz_backend.repository.QuizResultRepository;
import com.quiztournament.quiz_backend.repository.TournamentRepository;
import com.quiztournament.quiz_backend.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}