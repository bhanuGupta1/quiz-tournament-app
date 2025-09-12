package com.quiztournament.quiz_backend.dto;

import com.quiztournament.quiz_backend.entity.UserTournamentScore;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for quiz completion result
 * Contains final score, pass/fail status, and detailed results
 */
public class QuizResultResponse {

    private Long tournamentId;
    private String tournamentName;
    private Long userId;
    private String playerName;
    private Integer score;
    private Integer totalQuestions;
    private Double percentage;
    private Boolean passed;
    private Double minPassingScore;
    private LocalDateTime completedAt;
    private Map<Integer, String> userAnswers;
    private Map<Integer, String> correctAnswers;
    private Map<Integer, Boolean> answerResults;

    // Constructors
    public QuizResultResponse() {}

    public QuizResultResponse(UserTournamentScore userScore,
                              Map<Integer, String> userAnswers,
                              Map<Integer, String> correctAnswers,
                              Map<Integer, Boolean> answerResults) {
        this.tournamentId = userScore.getTournament().getId();
        this.tournamentName = userScore.getTournament().getName();
        this.userId = userScore.getUser().getId();
        this.playerName = userScore.getUser().getFirstName() + " " + userScore.getUser().getLastName();
        this.score = userScore.getScore();
        this.totalQuestions = 10; // Always 10 questions per tournament
        this.percentage = (userScore.getScore() / 10.0) * 100.0;
        this.passed = userScore.getPassed();
        this.minPassingScore = userScore.getTournament().getMinPassingScore();
        this.completedAt = userScore.getCompletedAt();
        this.userAnswers = userAnswers;
        this.correctAnswers = correctAnswers;
        this.answerResults = answerResults;
    }

    // Factory method for basic result (without detailed answers)
    public static QuizResultResponse fromUserTournamentScore(UserTournamentScore userScore) {
        QuizResultResponse response = new QuizResultResponse();
        response.setTournamentId(userScore.getTournament().getId());
        response.setTournamentName(userScore.getTournament().getName());
        response.setUserId(userScore.getUser().getId());
        
        // Debug logging
        System.out.println("Creating QuizResultResponse for user: " + 
            userScore.getUser().getFirstName() + " " + userScore.getUser().getLastName() + 
            " (ID: " + userScore.getUser().getId() + ")");
        
        response.setPlayerName(userScore.getUser().getFirstName() + " " + userScore.getUser().getLastName());
        response.setScore(userScore.getScore());
        response.setTotalQuestions(10);
        response.setPercentage((userScore.getScore() / 10.0) * 100.0);
        response.setPassed(userScore.getPassed());
        response.setMinPassingScore(userScore.getTournament().getMinPassingScore());
        response.setCompletedAt(userScore.getCompletedAt());
        return response;
    }

    // Getters and Setters
    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Double getMinPassingScore() {
        return minPassingScore;
    }

    public void setMinPassingScore(Double minPassingScore) {
        this.minPassingScore = minPassingScore;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Map<Integer, String> getUserAnswers() {
        return userAnswers;
    }

    public void setUserAnswers(Map<Integer, String> userAnswers) {
        this.userAnswers = userAnswers;
    }

    public Map<Integer, String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Map<Integer, String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Map<Integer, Boolean> getAnswerResults() {
        return answerResults;
    }

    public void setAnswerResults(Map<Integer, Boolean> answerResults) {
        this.answerResults = answerResults;
    }

    // Helper methods
    public String getGrade() {
        if (percentage >= 90) return "A+";
        else if (percentage >= 85) return "A";
        else if (percentage >= 80) return "B+";
        else if (percentage >= 75) return "B";
        else if (percentage >= 70) return "C+";
        else if (percentage >= 65) return "C";
        else if (percentage >= 60) return "D+";
        else if (percentage >= 50) return "D";
        else return "F";
    }

    public String getPerformanceMessage() {
        if (passed) {
            if (percentage >= 90) return "Outstanding performance! You're a quiz champion!";
            else if (percentage >= 80) return "Great job! You passed with flying colors!";
            else return "Well done! You successfully passed the tournament!";
        } else {
            return "You didn't pass this time, but keep practicing and try again!";
        }
    }

    public int getCorrectAnswerCount() {
        return score != null ? score : 0;
    }

    public int getIncorrectAnswerCount() {
        return totalQuestions - getCorrectAnswerCount();
    }

    @Override
    public String toString() {
        return "QuizResultResponse{" +
                "tournamentId=" + tournamentId +
                ", tournamentName='" + tournamentName + '\'' +
                ", playerName='" + playerName + '\'' +
                ", score=" + score +
                ", totalQuestions=" + totalQuestions +
                ", percentage=" + percentage +
                ", passed=" + passed +
                ", completedAt=" + completedAt +
                '}';
    }
}