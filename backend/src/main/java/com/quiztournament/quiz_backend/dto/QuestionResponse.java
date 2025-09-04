package com.quiztournament.quiz_backend.dto;

import java.util.List;

/**
 * DTO for question response to frontend
 * Cleaned and formatted question data for quiz participation
 */
public class QuestionResponse {

    private String question;
    private String type; // "multiple" or "boolean"
    private String difficulty;
    private String category;
    private List<String> answerOptions; // All possible answers in random order
    private Integer questionNumber; // Position in the quiz (1-10)
    private Integer totalQuestions; // Total questions in quiz (usually 10)

    // Note: correctAnswer is NOT included in response for security
    // The correct answer is stored server-side for validation

    // Constructors
    public QuestionResponse() {}

    public QuestionResponse(String question, String type, String difficulty, String category,
                            List<String> answerOptions, Integer questionNumber, Integer totalQuestions) {
        this.question = question;
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.answerOptions = answerOptions;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
    }

    // Factory method to create from OpenTDBQuestion
    public static QuestionResponse fromOpenTDBQuestion(OpenTDBQuestion openTDBQuestion,
                                                       Integer questionNumber,
                                                       Integer totalQuestions) {
        return new QuestionResponse(
                openTDBQuestion.getQuestion(),
                openTDBQuestion.getType(),
                openTDBQuestion.getDifficulty(),
                openTDBQuestion.getCategory(),
                openTDBQuestion.getAllAnswersShuffled(), // Randomize answer order
                questionNumber,
                totalQuestions
        );
    }

    // Factory method to create from OpenTDBQuestion with deterministic ordering
    public static QuestionResponse fromOpenTDBQuestionOrdered(OpenTDBQuestion openTDBQuestion,
                                                              Integer questionNumber,
                                                              Integer totalQuestions) {
        return new QuestionResponse(
                openTDBQuestion.getQuestion(),
                openTDBQuestion.getType(),
                openTDBQuestion.getDifficulty(),
                openTDBQuestion.getCategory(),
                openTDBQuestion.getAllAnswersOrdered(), // Consistent order for same user
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

    /**
     * Check if this is a true/false question
     * @return true if question type is boolean
     */
    public boolean isTrueFalseQuestion() {
        return "boolean".equalsIgnoreCase(type);
    }

    /**
     * Check if this is a multiple choice question
     * @return true if question type is multiple
     */
    public boolean isMultipleChoiceQuestion() {
        return "multiple".equalsIgnoreCase(type);
    }

    /**
     * Get the number of answer options
     * @return number of possible answers
     */
    public int getAnswerCount() {
        return answerOptions != null ? answerOptions.size() : 0;
    }

    /**
     * Check if this is the last question in the quiz
     * @return true if this is the final question
     */
    public boolean isLastQuestion() {
        return questionNumber != null && totalQuestions != null &&
                questionNumber.equals(totalQuestions);
    }

    /**
     * Get progress percentage
     * @return percentage completion of quiz
     */
    public double getProgressPercentage() {
        if (questionNumber == null || totalQuestions == null || totalQuestions == 0) {
            return 0.0;
        }
        return (questionNumber.doubleValue() / totalQuestions.doubleValue()) * 100.0;
    }

    @Override
    public String toString() {
        return "QuestionResponse{" +
                "question='" + question + '\'' +
                ", type='" + type + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", category='" + category + '\'' +
                ", answerOptions=" + answerOptions +
                ", questionNumber=" + questionNumber +
                ", totalQuestions=" + totalQuestions +
                '}';
    }
}