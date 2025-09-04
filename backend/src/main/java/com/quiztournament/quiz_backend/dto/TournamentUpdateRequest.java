package com.quiztournament.quiz_backend.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO for updating an existing tournament
 * Only allows updating name, start date, and end date as per requirements
 */
public class TournamentUpdateRequest {

    @Size(max = 100, message = "Tournament name cannot exceed 100 characters")
    private String name;

    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    // Constructors
    public TournamentUpdateRequest() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    // Custom validation method
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Allow partial updates
        }
        return endDate.isAfter(startDate);
    }
}