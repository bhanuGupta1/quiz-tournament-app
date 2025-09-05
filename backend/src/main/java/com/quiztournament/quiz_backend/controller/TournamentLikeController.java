package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.service.TournamentLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Tournament Like/Unlike functionality
 * Handles tournament popularity and user preferences
 */
@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "http://localhost:3000")
public class TournamentLikeController {

    @Autowired
    private TournamentLikeService tournamentLikeService;

    /**
     * Like a tournament (Player only)
     * POST /api/tournaments/{id}/like
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> likeTournament(@PathVariable Long id) {
        try {
            Map<String, Object> result = tournamentLikeService.likeTournament(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Unlike a tournament (Player only)
     * DELETE /api/tournaments/{id}/like
     */
    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> unlikeTournament(@PathVariable Long id) {
        try {
            Map<String, Object> result = tournamentLikeService.unlikeTournament(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Toggle like status for a tournament (Player only)
     * PUT /api/tournaments/{id}/like
     */
    @PutMapping("/{id}/like")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> toggleTournamentLike(@PathVariable Long id) {
        try {
            Map<String, Object> result = tournamentLikeService.toggleTournamentLike(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournament like status (Authenticated users)
     * GET /api/tournaments/{id}/like-status
     */
    @GetMapping("/{id}/like-status")
    public ResponseEntity<?> getTournamentLikeStatus(@PathVariable Long id) {
        try {
            Map<String, Object> result = tournamentLikeService.getTournamentLikeStatus(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournaments liked by current user (Player only)
     * GET /api/tournaments/my-likes
     */
    @GetMapping("/my-likes")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getUserLikedTournaments() {
        try {
            List<Map<String, Object>> likedTournaments = tournamentLikeService.getUserLikedTournaments();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("likedTournaments", likedTournaments);
            responseBody.put("count", likedTournaments.size());
            responseBody.put("message", "Your liked tournaments");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get most liked tournaments (All users)
     * GET /api/tournaments/popular
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getMostLikedTournaments(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> popularTournaments = tournamentLikeService.getMostLikedTournaments(
                    Math.min(limit, 50) // Max 50 results
            );

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("popularTournaments", popularTournaments);
            responseBody.put("count", popularTournaments.size());
            responseBody.put("message", "Most popular tournaments");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get tournament like statistics (Admin only)
     * GET /api/tournaments/{id}/like-statistics
     */
    @GetMapping("/{id}/like-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentLikeStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> statistics = tournamentLikeService.getTournamentLikeStatistics(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("statistics", statistics);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get user's like statistics (Player only)
     * GET /api/tournaments/my-like-statistics
     */
    @GetMapping("/my-like-statistics")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getUserLikeStatistics() {
        try {
            Map<String, Object> statistics = tournamentLikeService.getUserLikeStatistics();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("statistics", statistics);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Check like eligibility for tournament (Player only)
     * GET /api/tournaments/{id}/like-eligibility
     */
    @GetMapping("/{id}/like-eligibility")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> checkLikeEligibility(@PathVariable Long id) {
        try {
            Map<String, Object> eligibility = tournamentLikeService.checkLikeEligibility(id);

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
     * Health check for like service
     * GET /api/tournaments/likes/health
     */
    @GetMapping("/likes/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tournament Like service is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}