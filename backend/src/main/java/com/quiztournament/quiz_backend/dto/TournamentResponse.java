package com.quiztournament.quiz_backend.dto;

import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.TournamentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for tournament response data
 * Contains all tournament information for API responses
 */
public class TournamentResponse {

    private Long id;
    private String name;
    private String category;
    private String difficulty;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double minPassingScore;
    private TournamentStatus status;
    private String createdBy;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics fields
    private Long participantCount;
    private Double averageScore;
    private Long likeCount;

    // Constructors
    public TournamentResponse() {}

    public TournamentResponse(Tournament tournament) {
        this.id = tournament.getId();
        this.name = tournament.getName();
        this.category = tournament.getCategory();
        this.difficulty = tournament.getDifficulty();
        this.startDate = tournament.getStartDate();
        this.endDate = tournament.getEndDate();
        this.minPassingScore = tournament.getMinPassingScore();
        this.status = tournament.getStatus();
        this.createdAt = tournament.getCreatedAt();
        this.updatedAt = tournament.getUpdatedAt();

        if (tournament.getCreatedBy() != null) {
            this.createdBy = tournament.getCreatedBy().getUsername();
            this.createdById = tournament.getCreatedBy().getId();
        }
    }

    // Factory method with statistics
    public static TournamentResponse withStatistics(Tournament tournament,
                                                    Long participantCount,
                                                    Double averageScore,
                                                    Long likeCount) {
        TournamentResponse response = new TournamentResponse(tournament);
        response.setParticipantCount(participantCount);
        response.setAverageScore(averageScore);
        response.setLikeCount(likeCount);
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getMinPassingScore() {
        return minPassingScore;
    }

    public void setMinPassingScore(Double minPassingScore) {
        this.minPassingScore = minPassingScore;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(Long participantCount) {
        this.participantCount = participantCount;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
}