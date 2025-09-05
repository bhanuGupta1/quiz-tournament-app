package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.TournamentLike;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.repository.TournamentLikeRepository;
import com.quiztournament.quiz_backend.repository.TournamentRepository;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling tournament like/unlike functionality
 * Manages user preferences and tournament popularity tracking
 */
@Service
@Transactional
public class TournamentLikeService {

    @Autowired
    private TournamentLikeRepository tournamentLikeRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Like a tournament
     * @param tournamentId Tournament ID to like
     * @return Like operation result
     */
    public Map<String, Object> likeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        // Check if user has already liked this tournament
        Optional<TournamentLike> existingLike = tournamentLikeRepository.findByUserAndTournament(currentUser, tournament);

        if (existingLike.isPresent()) {
            throw new RuntimeException("You have already liked this tournament");
        }

        // Create new like
        TournamentLike tournamentLike = new TournamentLike(currentUser, tournament);
        tournamentLikeRepository.save(tournamentLike);

        // Get updated like count
        long totalLikes = tournamentLikeRepository.countByTournament(tournament);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tournament liked successfully");
        response.put("tournamentId", tournamentId);
        response.put("tournamentName", tournament.getName());
        response.put("totalLikes", totalLikes);
        response.put("userLiked", true);

        return response;
    }

    /**
     * Unlike a tournament
     * @param tournamentId Tournament ID to unlike
     * @return Unlike operation result
     */
    public Map<String, Object> unlikeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        // Check if user has liked this tournament
        Optional<TournamentLike> existingLike = tournamentLikeRepository.findByUserAndTournament(currentUser, tournament);

        if (existingLike.isEmpty()) {
            throw new RuntimeException("You have not liked this tournament yet");
        }

        // Remove like
        tournamentLikeRepository.delete(existingLike.get());

        // Get updated like count
        long totalLikes = tournamentLikeRepository.countByTournament(tournament);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tournament unliked successfully");
        response.put("tournamentId", tournamentId);
        response.put("tournamentName", tournament.getName());
        response.put("totalLikes", totalLikes);
        response.put("userLiked", false);

        return response;
    }

    /**
     * Toggle like status for a tournament
     * @param tournamentId Tournament ID
     * @return Toggle operation result
     */
    public Map<String, Object> toggleTournamentLike(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        Optional<TournamentLike> existingLike = tournamentLikeRepository.findByUserAndTournament(currentUser, tournament);

        if (existingLike.isPresent()) {
            // Unlike the tournament
            tournamentLikeRepository.delete(existingLike.get());
            long totalLikes = tournamentLikeRepository.countByTournament(tournament);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("action", "unliked");
            response.put("message", "Tournament unliked successfully");
            response.put("tournamentId", tournamentId);
            response.put("tournamentName", tournament.getName());
            response.put("totalLikes", totalLikes);
            response.put("userLiked", false);
            return response;
        } else {
            // Like the tournament
            TournamentLike tournamentLike = new TournamentLike(currentUser, tournament);
            tournamentLikeRepository.save(tournamentLike);
            long totalLikes = tournamentLikeRepository.countByTournament(tournament);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("action", "liked");
            response.put("message", "Tournament liked successfully");
            response.put("tournamentId", tournamentId);
            response.put("tournamentName", tournament.getName());
            response.put("totalLikes", totalLikes);
            response.put("userLiked", true);
            return response;
        }
    }

    /**
     * Get tournament like status for current user
     * @param tournamentId Tournament ID
     * @return Like status and count
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTournamentLikeStatus(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        boolean userLiked = tournamentLikeRepository.existsByUserAndTournament(currentUser, tournament);
        long totalLikes = tournamentLikeRepository.countByTournament(tournament);

        Map<String, Object> response = new HashMap<>();
        response.put("tournamentId", tournamentId);
        response.put("tournamentName", tournament.getName());
        response.put("userLiked", userLiked);
        response.put("totalLikes", totalLikes);
        response.put("success", true);

        return response;
    }

    /**
     * Get tournaments liked by current user
     * @return List of liked tournaments
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserLikedTournaments() {
        User currentUser = getCurrentUser();
        List<TournamentLike> likes = tournamentLikeRepository.findByUserOrderByCreatedAtDesc(currentUser);

        return likes.stream().map(like -> {
            Map<String, Object> tournamentInfo = new HashMap<>();
            Tournament tournament = like.getTournament();

            tournamentInfo.put("tournamentId", tournament.getId());
            tournamentInfo.put("tournamentName", tournament.getName());
            tournamentInfo.put("category", tournament.getCategory());
            tournamentInfo.put("difficulty", tournament.getDifficulty());
            tournamentInfo.put("status", tournament.getStatus().name());
            tournamentInfo.put("likedAt", like.getCreatedAt());
            tournamentInfo.put("totalLikes", tournamentLikeRepository.countByTournament(tournament));

            return tournamentInfo;
        }).collect(Collectors.toList());
    }

    /**
     * Get most liked tournaments
     * @param limit Maximum number of tournaments to return
     * @return List of popular tournaments
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMostLikedTournaments(int limit) {
        List<Object[]> results = tournamentLikeRepository.findMostLikedTournaments();

        return results.stream()
                .limit(limit)
                .map(result -> {
                    Tournament tournament = (Tournament) result[0];
                    Long likeCount = (Long) result[1];

                    Map<String, Object> tournamentInfo = new HashMap<>();
                    tournamentInfo.put("tournamentId", tournament.getId());
                    tournamentInfo.put("tournamentName", tournament.getName());
                    tournamentInfo.put("category", tournament.getCategory());
                    tournamentInfo.put("difficulty", tournament.getDifficulty());
                    tournamentInfo.put("status", tournament.getStatus().name());
                    tournamentInfo.put("likeCount", likeCount);
                    tournamentInfo.put("createdBy", tournament.getCreatedBy().getUsername());

                    return tournamentInfo;
                }).collect(Collectors.toList());
    }

    /**
     * Get tournament like statistics
     * @param tournamentId Tournament ID
     * @return Detailed like statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTournamentLikeStatistics(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        List<TournamentLike> likes = tournamentLikeRepository.findByTournament(tournament);

        Map<String, Object> stats = new HashMap<>();
        stats.put("tournamentId", tournamentId);
        stats.put("tournamentName", tournament.getName());
        stats.put("totalLikes", likes.size());

        if (!likes.isEmpty()) {
            // Group likes by date for trend analysis
            Map<String, Long> dailyLikes = likes.stream()
                    .collect(Collectors.groupingBy(
                            like -> like.getCreatedAt().toLocalDate().toString(),
                            Collectors.counting()
                    ));

            stats.put("dailyLikeTrends", dailyLikes);
            stats.put("firstLikedAt", likes.get(likes.size() - 1).getCreatedAt());
            stats.put("lastLikedAt", likes.get(0).getCreatedAt());
        }

        return stats;
    }

    /**
     * Get user's like statistics
     * @return User's liking activity statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getUserLikeStatistics() {
        User currentUser = getCurrentUser();

        long totalLikes = tournamentLikeRepository.countByUser(currentUser);
        List<TournamentLike> likes = tournamentLikeRepository.findByUser(currentUser);

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", currentUser.getId());
        stats.put("username", currentUser.getUsername());
        stats.put("totalLikes", totalLikes);

        if (!likes.isEmpty()) {
            // Analyze liked categories
            Map<String, Long> likedCategories = likes.stream()
                    .collect(Collectors.groupingBy(
                            like -> like.getTournament().getCategory(),
                            Collectors.counting()
                    ));

            // Analyze liked difficulties
            Map<String, Long> likedDifficulties = likes.stream()
                    .collect(Collectors.groupingBy(
                            like -> like.getTournament().getDifficulty(),
                            Collectors.counting()
                    ));

            stats.put("favoriteCategories", likedCategories);
            stats.put("favoriteDifficulties", likedDifficulties);
            stats.put("firstLike", likes.get(likes.size() - 1).getCreatedAt());
            stats.put("lastLike", likes.get(0).getCreatedAt());
        }

        return stats;
    }

    /**
     * Remove all likes for a tournament (used when tournament is deleted)
     * @param tournamentId Tournament ID
     */
    public void removeAllLikesForTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        List<TournamentLike> likes = tournamentLikeRepository.findByTournament(tournament);
        tournamentLikeRepository.deleteAll(likes);

        System.out.println("Removed " + likes.size() + " likes for tournament: " + tournament.getName());
    }

    /**
     * Check if current user can like a tournament
     * @param tournamentId Tournament ID
     * @return Eligibility information
     */
    @Transactional(readOnly = true)
    public Map<String, Object> checkLikeEligibility(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        boolean alreadyLiked = tournamentLikeRepository.existsByUserAndTournament(currentUser, tournament);
        boolean canLike = !alreadyLiked; // Users can like any tournament they haven't liked yet

        Map<String, Object> eligibility = new HashMap<>();
        eligibility.put("canLike", canLike);
        eligibility.put("alreadyLiked", alreadyLiked);
        eligibility.put("tournamentId", tournamentId);
        eligibility.put("tournamentName", tournament.getName());
        eligibility.put("message", alreadyLiked ? "You have already liked this tournament" : "You can like this tournament");

        return eligibility;
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