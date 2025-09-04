package com.quiztournament.quiz_backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for creating a new tournament
 */
public class TournamentCreateRequest {

    @NotBlank(message = "Tournament name is required")
    @Size(max = 100, message = "Tournament name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Difficulty is required")
    @Pattern(regexp = "easy|medium|hard", message = "Difficulty must be 'easy', 'medium', or 'hard'")
    private String difficulty;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Minimum passing score is required")
    @DecimalMin(value = "0.0", message = "Minimum passing score must be at least 0%")
    @DecimalMax(value = "100.0", message = "Minimum passing score cannot exceed 100%")
    private Double minPassingScore;

    // Constructors
    public TournamentCreateRequest() {}

    // Getters and Setters
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

    // Custom validation method
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let other validations handle null values
        }
        return endDate.isAfter(startDate);
    }
}