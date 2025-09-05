package com.quiztournament.quiz_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for quiz answer submission
 */
public class QuizAnswerRequest {

    @NotNull(message = "Question number is required")
    private Integer questionNumber;

    @NotBlank(message = "Answer is required")
    private String answer;

    // Constructors
    public QuizAnswerRequest() {}

    public QuizAnswerRequest(Integer questionNumber, String answer) {
        this.questionNumber = questionNumber;
        this.answer = answer;
    }

    // Getters and Setters
    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "QuizAnswerRequest{" +
                "questionNumber=" + questionNumber +
                ", answer='" + answer + '\'' +
                '}';
    }
}