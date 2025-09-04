package com.quiztournament.quiz_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * DTO for individual question from OpenTDB API
 * Represents a single quiz question with answers
 */
public class OpenTDBQuestion {

    @JsonProperty("category")
    private String category;

    @JsonProperty("type")
    private String type; // "multiple" or "boolean"

    @JsonProperty("difficulty")
    private String difficulty; // "easy", "medium", "hard"

    @JsonProperty("question")
    private String question;

    @JsonProperty("correct_answer")
    private String correctAnswer;

    @JsonProperty("incorrect_answers")
    private List<String> incorrectAnswers;

    // Constructors
    public OpenTDBQuestion() {
        this.incorrectAnswers = new ArrayList<>();
    }

    public OpenTDBQuestion(String category, String type, String difficulty,
                           String question, String correctAnswer, List<String> incorrectAnswers) {
        this.category = category;
        this.type = type;
        this.difficulty = difficulty;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.incorrectAnswers = incorrectAnswers != null ? incorrectAnswers : new ArrayList<>();
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
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
        this.incorrectAnswers = incorrectAnswers != null ? incorrectAnswers : new ArrayList<>();
    }

    // Business logic methods

    /**
     * Get all answer options in random order
     * @return List of all answers shuffled
     */
    public List<String> getAllAnswersShuffled() {
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(correctAnswer);
        if (incorrectAnswers != null) {
            allAnswers.addAll(incorrectAnswers);
        }
        Collections.shuffle(allAnswers);
        return allAnswers;
    }

    /**
     * Get all answer options in deterministic order (for consistent display)
     * @return List of all answers in consistent order
     */
    public List<String> getAllAnswersOrdered() {
        List<String> allAnswers = new ArrayList<>();
        allAnswers.add(correctAnswer);
        if (incorrectAnswers != null) {
            allAnswers.addAll(incorrectAnswers);
        }
        Collections.sort(allAnswers); // Alphabetical order for consistency
        return allAnswers;
    }

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
     * Check if the provided answer is correct
     * @param answer The answer to check
     * @return true if answer matches correct answer
     */
    public boolean isCorrectAnswer(String answer) {
        return correctAnswer != null && correctAnswer.equalsIgnoreCase(answer);
    }

    /**
     * Get the total number of answer options
     * @return total number of possible answers
     */
    public int getAnswerCount() {
        return 1 + (incorrectAnswers != null ? incorrectAnswers.size() : 0);
    }

    @Override
    public String toString() {
        return "OpenTDBQuestion{" +
                "category='" + category + '\'' +
                ", type='" + type + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", question='" + question + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", incorrectAnswers=" + incorrectAnswers +
                '}';
    }
}