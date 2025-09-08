package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import com.quiztournament.quiz_backend.dto.TournamentUpdateRequest;
import com.quiztournament.quiz_backend.dto.TournamentResponse;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.TournamentStatus;
import com.quiztournament.quiz_backend.repository.TournamentRepository;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for Tournament operations
 * Handles business logic for tournament CRUD operations and statistics
 */
@Service
@Transactional
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Create a new tournament
     * @param request Tournament creation data
     * @return Created tournament response
     */
    public TournamentResponse createTournament(TournamentCreateRequest request) {
        // Get current authenticated admin user
        User currentUser = getCurrentUser();

        // Validate that user is an admin (should be handled by security, but double-check)
        if (!currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Only admin users can create tournaments");
        }

        // Create tournament entity
        Tournament tournament = new Tournament();
        tournament.setName(request.getName());
        tournament.setCategory(request.getCategory());
        tournament.setDifficulty(request.getDifficulty());
        tournament.setStartDate(request.getStartDate());
        tournament.setEndDate(request.getEndDate());
        tournament.setMinPassingScore(request.getMinPassingScore());
        tournament.setCreatedBy(currentUser);

        // Save tournament
        Tournament savedTournament = tournamentRepository.save(tournament);

        // Send email notifications to all players
        try {
            emailService.sendNewTournamentNotification(savedTournament, currentUser);
        } catch (Exception e) {
            System.err.println("Failed to send tournament notification emails: " + e.getMessage());
            // Don't fail tournament creation if email fails
        }

        return new TournamentResponse(savedTournament);
    }

    /**
     * Get all tournaments with statistics
     * @return List of tournament responses with stats
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getAllTournaments() {
        List<Tournament> tournaments = tournamentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return tournaments.stream()
                .map(this::convertToResponseWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Get tournament by ID
     * @param id Tournament ID
     * @return Tournament response
     */
    @Transactional(readOnly = true)
    public TournamentResponse getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));

        return convertToResponseWithStats(tournament);
    }

    /**
     * Update tournament
     * @param id Tournament ID
     * @param request Update request data
     * @return Updated tournament response
     */
    public TournamentResponse updateTournament(Long id, TournamentUpdateRequest request) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));

        // Validate that the current user is the creator or an admin
        User currentUser = getCurrentUser();
        if (!tournament.getCreatedBy().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("You can only update tournaments you created");
        }

        // Update fields if provided
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            tournament.setName(request.getName());
        }
        if (request.getStartDate() != null) {
            tournament.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            tournament.setEndDate(request.getEndDate());
        }

        Tournament updatedTournament = tournamentRepository.save(tournament);
        return new TournamentResponse(updatedTournament);
    }

    /**
     * Delete tournament
     * @param id Tournament ID
     */
    public void deleteTournament(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));

        // Validate that the current user is the creator or an admin
        User currentUser = getCurrentUser();
        if (!tournament.getCreatedBy().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("You can only delete tournaments you created");
        }

        // Check if tournament has participants (optional business rule)
        Long participantCount = tournamentRepository.countParticipants(id);
        if (participantCount > 0) {
            throw new RuntimeException("Cannot delete tournament with existing participants");
        }

        tournamentRepository.delete(tournament);
    }

    /**
     * Get tournaments by status
     * @param status Tournament status
     * @return List of tournaments with specified status
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getTournamentsByStatus(TournamentStatus status) {
        LocalDate currentDate = LocalDate.now();
        List<Tournament> tournaments;

        switch (status) {
            case UPCOMING:
                tournaments = tournamentRepository.findUpcomingTournaments(currentDate);
                break;
            case ONGOING:
                tournaments = tournamentRepository.findOngoingTournaments(currentDate);
                break;
            case PAST:
                tournaments = tournamentRepository.findPastTournaments(currentDate);
                break;
            default:
                tournaments = tournamentRepository.findAll();
        }

        return tournaments.stream()
                .map(this::convertToResponseWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Get tournaments created by current admin user
     * @return List of tournaments created by current user
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getMyTournaments() {
        User currentUser = getCurrentUser();
        List<Tournament> tournaments = tournamentRepository.findByCreatedBy(currentUser);

        return tournaments.stream()
                .map(this::convertToResponseWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Get tournaments by category
     * @param category Tournament category
     * @return List of tournaments in specified category
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getTournamentsByCategory(String category) {
        List<Tournament> tournaments = tournamentRepository.findByCategory(category);

        return tournaments.stream()
                .map(this::convertToResponseWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Get tournaments by difficulty
     * @param difficulty Tournament difficulty
     * @return List of tournaments with specified difficulty
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getTournamentsByDifficulty(String difficulty) {
        List<Tournament> tournaments = tournamentRepository.findByDifficulty(difficulty);

        return tournaments.stream()
                .map(this::convertToResponseWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Convert tournament to response with statistics
     * @param tournament Tournament entity
     * @return Tournament response with stats
     */
    private TournamentResponse convertToResponseWithStats(Tournament tournament) {
        // Get statistics
        Long participantCount = tournamentRepository.countParticipants(tournament.getId());
        Double averageScore = tournamentRepository.getAverageScore(tournament.getId());
        Long likeCount = tournamentRepository.countLikes(tournament.getId());

        return TournamentResponse.withStatistics(tournament, participantCount, averageScore, likeCount);
    }

    /**
     * Get current authenticated user
     * @return Current user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();

        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    /**
     * Check if tournament exists
     * @param id Tournament ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(Long id) {
        return tournamentRepository.existsById(id);
    }

    /**
     * Get total tournament count
     * @return Total number of tournaments
     */
    @Transactional(readOnly = true)
    public long getTotalTournamentCount() {
        return tournamentRepository.count();
    }
}