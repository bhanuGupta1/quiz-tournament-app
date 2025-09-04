package com.quiztournament.quiz_backend.repository;

import com.quiztournament.quiz_backend.entity.TournamentLike;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TournamentLike entity
 * Handles like/unlike functionality for tournaments
 */
@Repository
public interface TournamentLikeRepository extends JpaRepository<TournamentLike, Long> {

    // Find specific like by user and tournament
    Optional<TournamentLike> findByUserAndTournament(User user, Tournament tournament);

    // Check if user has liked a tournament
    boolean existsByUserAndTournament(User user, Tournament tournament);

    // Find all likes for a specific tournament
    List<TournamentLike> findByTournament(Tournament tournament);

    // Find all tournaments liked by a user
    List<TournamentLike> findByUser(User user);

    // Count total likes for a tournament
    long countByTournament(Tournament tournament);

    // Count total likes by a user
    long countByUser(User user);

    // Delete like by user and tournament (for unlike functionality)
    void deleteByUserAndTournament(User user, Tournament tournament);

    // Get tournaments with most likes (popular tournaments)
    @Query("SELECT tl.tournament, COUNT(tl) as likeCount FROM TournamentLike tl " +
            "GROUP BY tl.tournament ORDER BY likeCount DESC")
    List<Object[]> findMostLikedTournaments();

    // Get tournaments liked by a user ordered by like date
    @Query("SELECT tl FROM TournamentLike tl WHERE tl.user = :user ORDER BY tl.createdAt DESC")
    List<TournamentLike> findByUserOrderByCreatedAtDesc(@Param("user") User user);
}