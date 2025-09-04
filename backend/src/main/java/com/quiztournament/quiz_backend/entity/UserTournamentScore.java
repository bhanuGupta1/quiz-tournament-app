package com.quiztournament.quiz_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * Entity representing a user's score for a specific tournament
 * Tracks quiz completion, scores, and pass/fail status
 */
@Entity
@Table(name = "user_tournament_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tournament_id"}))
public class UserTournamentScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Min(value = 0, message = "Score cannot be negative")
    @Max(value = 10, message = "Score cannot exceed 10")
    @Column(nullable = false)
    private Integer score; // Score out of 10

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "passed", nullable = false)
    private Boolean passed; // Did they meet minimum passing score?

    // Constructors
    public UserTournamentScore() {}

    public UserTournamentScore(User user, Tournament tournament, Integer score) {
        this.user = user;
        this.tournament = tournament;
        this.score = score;
        this.completedAt = LocalDateTime.now();
        this.passed = calculatePassed(score, tournament.getMinPassingScore());
    }

    // Business logic to determine if user passed
    private Boolean calculatePassed(Integer score, Double minPassingScore) {
        if (score == null || minPassingScore == null) {
            return false;
        }
        double percentage = (score / 10.0) * 100;
        return percentage >= minPassingScore;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
        // Recalculate passed status when score changes
        if (this.tournament != null) {
            this.passed = calculatePassed(score, tournament.getMinPassingScore());
        }
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }
}