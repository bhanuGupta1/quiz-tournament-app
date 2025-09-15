package com.quiztournament.quiz_backend.controller;

import com.quiztournament.quiz_backend.dto.AdminQuestionResponse;
import com.quiztournament.quiz_backend.dto.QuestionResponse;
import com.quiztournament.quiz_backend.service.QuestionService;
import com.quiztournament.quiz_backend.service.OpenTDBService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Question operations
 * Handles quiz questions, answers validation, and quiz session management
 */
@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "http://localhost:3000")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private OpenTDBService openTDBService;

    /**
     * Get all questions for a tournament (Player only)
     * GET /api/tournaments/{id}/questions
     */
    @GetMapping("/{id}/questions")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getTournamentQuestions(@PathVariable Long id) {
        try {
            List<QuestionResponse> questions = questionService.getTournamentQuestions(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("questions", questions);
            responseBody.put("tournamentId", id);
            responseBody.put("totalQuestions", questions.size());
            responseBody.put("success", true);
            responseBody.put("message", "Quiz started! Good luck!");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get a specific question by number (Player only)
     * GET /api/tournaments/{id}/questions/{questionNumber}
     */
    @GetMapping("/{id}/questions/{questionNumber}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getQuestionByNumber(@PathVariable Long id,
                                                 @PathVariable Integer questionNumber) {
        try {
            QuestionResponse question = questionService.getQuestionByNumber(id, questionNumber);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("question", question);
            responseBody.put("tournamentId", id);
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Submit answer for a question (Player only)
     * POST /api/tournaments/{id}/questions/{questionNumber}/answer
     */
    @PostMapping("/{id}/questions/{questionNumber}/answer")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> submitAnswer(@PathVariable Long id,
                                          @PathVariable Integer questionNumber,
                                          @Valid @RequestBody AnswerSubmissionRequest request) {
        try {
            QuestionService.AnswerValidationResult result = questionService.validateAnswer(
                    id, questionNumber, request.getAnswer()
            );

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("correct", result.isCorrect());
            responseBody.put("correctAnswer", result.getCorrectAnswer());
            responseBody.put("userAnswer", result.getUserAnswer());
            responseBody.put("questionNumber", result.getQuestionNumber());
            responseBody.put("currentScore", result.getCorrectCount());
            responseBody.put("totalQuestions", result.getTotalQuestions());
            responseBody.put("success", true);

            if (result.isCorrect()) {
                responseBody.put("message", "Correct! Well done!");
            } else {
                responseBody.put("message", "Incorrect. The correct answer was: " + result.getCorrectAnswer());
            }

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get quiz session status (Player only)
     * GET /api/tournaments/{id}/session
     */
    @GetMapping("/{id}/session")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getQuizSessionStatus(@PathVariable Long id) {
        try {
            QuestionService.QuizSessionStatus status = questionService.getQuizSessionStatus(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("active", status.isActive());
            responseBody.put("currentQuestion", status.getCurrentQuestion());
            responseBody.put("correctAnswers", status.getCorrectAnswers());
            responseBody.put("totalQuestions", status.getTotalQuestions());
            responseBody.put("completed", status.isCompleted());
            responseBody.put("tournamentId", id);
            responseBody.put("success", true);

            if (status.isCompleted()) {
                responseBody.put("message", "Quiz completed! Submit to see your final score.");
            } else if (status.isActive()) {
                responseBody.put("message", "Quiz in progress. Question " + status.getCurrentQuestion() + " of " + status.getTotalQuestions());
            } else {
                responseBody.put("message", "No active quiz session found.");
            }

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Complete quiz and get final results (Player only)
     * POST /api/tournaments/{id}/complete
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> completeQuiz(@PathVariable Long id) {
        try {
            QuestionService.QuizCompletionResult result = questionService.completeQuiz(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("score", result.getCorrectAnswers());
            responseBody.put("totalQuestions", result.getTotalQuestions());
            responseBody.put("percentage", Math.round(result.getPercentage() * 100.0) / 100.0);
            responseBody.put("passed", result.isPassed());
            responseBody.put("minPassingScore", result.getMinPassingScore());
            responseBody.put("tournamentId", id);
            responseBody.put("success", true);

            // Format answer history for frontend
            List<Map<String, Object>> formattedAnswerHistory = new ArrayList<>();
            Map<Integer, QuestionService.UserAnswer> answerHistory = result.getAnswerHistory();
            
            for (int i = 1; i <= result.getTotalQuestions(); i++) {
                Map<String, Object> answerData = new HashMap<>();
                QuestionService.UserAnswer userAnswer = answerHistory.get(i);
                
                if (userAnswer != null) {
                    answerData.put("correct", userAnswer.isCorrect());
                    answerData.put("userAnswer", userAnswer.getAnswer());
                    answerData.put("correctAnswer", userAnswer.getCorrectAnswer());
                    answerData.put("question", userAnswer.getQuestion());
                } else {
                    // Handle case where no answer was provided
                    answerData.put("correct", false);
                    answerData.put("userAnswer", "");
                    answerData.put("correctAnswer", "");
                    answerData.put("question", "");
                }
                
                formattedAnswerHistory.add(answerData);
            }
            
            responseBody.put("answerHistory", formattedAnswerHistory);

            if (result.isPassed()) {
                responseBody.put("message", "Congratulations! You passed the quiz with " +
                        result.getCorrectAnswers() + "/" + result.getTotalQuestions() + " correct answers!");
            } else {
                responseBody.put("message", "Quiz completed. You scored " +
                        result.getCorrectAnswers() + "/" + result.getTotalQuestions() +
                        ". You need " + result.getMinPassingScore() + "% to pass.");
            }

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get available categories for tournaments (Admin only)
     * GET /api/tournaments/categories
     */
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAvailableCategories() {
        try {
            Map<String, String> categories = openTDBService.getAvailableCategories();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("categories", categories);
            responseBody.put("count", categories.size());
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Test OpenTDB API connectivity (Admin only)
     * GET /api/tournaments/test-api
     */
    @GetMapping("/test-api")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testApiConnectivity() {
        try {
            boolean connected = openTDBService.testApiConnectivity();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("connected", connected);
            responseBody.put("apiUrl", "https://opentdb.com/api.php");
            responseBody.put("success", true);

            if (connected) {
                responseBody.put("message", "OpenTDB API is accessible and working correctly.");
            } else {
                responseBody.put("message", "OpenTDB API is not accessible. Using fallback questions.");
            }

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            errorResponse.put("connected", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get question cache statistics (Admin only)
     * GET /api/tournaments/cache-stats
     */
    @GetMapping("/cache-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCacheStatistics() {
        try {
            Map<String, Object> stats = questionService.getCacheStatistics();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("statistics", stats);
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Clear question cache for a tournament (Admin only)
     * DELETE /api/tournaments/{id}/cache
     */
    @DeleteMapping("/{id}/cache")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clearTournamentCache(@PathVariable Long id) {
        try {
            questionService.clearTournamentCache(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Cache cleared for tournament " + id);
            responseBody.put("tournamentId", id);
            responseBody.put("success", true);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all questions for a tournament with answers (Admin only) - Assessment Requirement
     * GET /api/tournaments/{id}/questions/admin
     */
    @GetMapping("/{id}/questions/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTournamentQuestionsForAdmin(@PathVariable Long id) {
        try {
            List<AdminQuestionResponse> questions = questionService.getTournamentQuestionsWithAnswers(id);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("questions", questions);
            responseBody.put("tournamentId", id);
            responseBody.put("totalQuestions", questions.size());
            responseBody.put("success", true);
            responseBody.put("message", "Tournament questions retrieved for admin review");

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("success", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check for question service
     * GET /api/tournaments/questions/health
     */
    @GetMapping("/questions/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Question service is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("apiConnected", openTDBService.testApiConnectivity());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // Inner class for answer submission request
    public static class AnswerSubmissionRequest {
        private String answer;

        public AnswerSubmissionRequest() {}

        public AnswerSubmissionRequest(String answer) {
            this.answer = answer;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}