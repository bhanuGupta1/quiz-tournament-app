package com.quiztournament.quiz_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing individual quiz question answers
 * Stores detailed answer information for admin review
 */
@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_result_id", nullable = false)
    private QuizResult quizResult;
    
    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;
    
    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;
    
    @Column(name = "user_answer", nullable = false, length = 500)
    private String userAnswer;
    
    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;
    
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
    
    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
    
    // Constructors
    public QuizAnswer() {}
    
    public QuizAnswer(QuizResult quizResult, Integer questionNumber, String questionText,
                     String userAnswer, String correctAnswer, Boolean isCorrect) {
        this.quizResult = quizResult;
        this.questionNumber = questionNumber;
        this.questionText = questionText;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.answeredAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public QuizResult getQuizResult() {
        return quizResult;
    }
    
    public void setQuizResult(QuizResult quizResult) {
        this.quizResult = quizResult;
    }
    
    public Integer getQuestionNumber() {
        return questionNumber;
    }
    
    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getUserAnswer() {
        return userAnswer;
    }
    
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public Boolean getIsCorrect() {
        return isCorrect;
    }
    
    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    
    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
    
    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}