package com.quiztournament.quiz_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a completed quiz result
 */
@Entity
@Table(name = "quiz_results")
public class QuizResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    
    @Column(name = "score", nullable = false)
    private Integer score;
    
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;
    
    @Column(name = "percentage", nullable = false)
    private Double percentage;
    
    @Column(name = "passed", nullable = false)
    private Boolean passed;
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;
    
    // Constructors
    public QuizResult() {}
    
    public QuizResult(User user, Tournament tournament, Integer score, Integer totalQuestions, 
                     Double percentage, Boolean passed, Integer timeTakenSeconds) {
        this.user = user;
        this.tournament = tournament;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.passed = passed;
        this.timeTakenSeconds = timeTakenSeconds;
        this.completedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public Integer getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(Integer timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }
}