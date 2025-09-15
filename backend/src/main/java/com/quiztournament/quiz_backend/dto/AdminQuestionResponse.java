package com.quiztournament.quiz_backend.dto;

import java.util.List;

/**
 * DTO for admin question response with correct answers
 * Used for tournament management and question review
 */
public class AdminQuestionResponse {

    private String question;
    private String type; // "multiple" or "boolean"
    private String difficulty;
    private String category;
    private List<String> answerOptions; // All possible answers
    private String correctAnswer; // The correct answer (admin only)
    private List<String> incorrectAnswers; // All incorrect answers (admin only)
    private Integer questionNumber; // Position in the quiz (1-10)
    private Integer totalQuestions; // Total questions in quiz (usually 10)

    // Constructors
    public AdminQuestionResponse() {}

    public AdminQuestionResponse(String question, String type, String difficulty, String category,
                                List<String> answerOptions, String correctAnswer, 
                                List<String> incorrectAnswers, Integer questionNumber, Integer totalQuestions) {
        this.question = question;
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.answerOptions = answerOptions;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
    }

    // Factory method to create from OpenTDBQuestion for admin viewing
    public static AdminQuestionResponse fromOpenTDBQuestion(OpenTDBQuestion openTDBQuestion,
                                                           Integer questionNumber,
                                                           Integer totalQuestions) {
        return new AdminQuestionResponse(
                openTDBQuestion.getQuestion(),
                openTDBQuestion.getType(),
                openTDBQuestion.getDifficulty(),
                openTDBQuestion.getCategory(),
                openTDBQuestion.getAllAnswersOrdered(),
                openTDBQuestion.getCorrectAnswer(),
                openTDBQuestion.getIncorrectAnswers(),
                questionNumber,
                totalQuestions
        );
    }

    // Getters and Setters
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    public void setAnswerOptions(List<String> answerOptions) {
        this.answerOptions = answerOptions;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    // Helper methods
    public boolean isTrueFalseQuestion() {
        return "boolean".equalsIgnoreCase(type);
    }

    public boolean isMultipleChoiceQuestion() {
        return "multiple".equalsIgnoreCase(type);
    }

    public int getAnswerCount() {
        return answerOptions != null ? answerOptions.size() : 0;
    }

    @Override
    public String toString() {
        return "AdminQuestionResponse{" +
                "question='" + question + '\'' +
                ", type='" + type + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", category='" + category + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", questionNumber=" + questionNumber +
                ", totalQuestions=" + totalQuestions +
                '}';
    }
}