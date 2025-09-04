package com.quiztournament.quiz_backend.entity;

/**
 * Enum representing the current status of a tournament
 * UPCOMING: Tournament hasn't started yet
 * ONGOING: Tournament is currently active
 * PAST: Tournament has ended
 */
public enum TournamentStatus {
    UPCOMING,
    ONGOING,
    PAST
}