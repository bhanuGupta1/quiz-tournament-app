package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.dto.OpenTDBQuestion;
import com.quiztournament.quiz_backend.dto.QuestionResponse;
import com.quiztournament.quiz_backend.entity.QuizResult;
import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserTournamentScore;
import com.quiztournament.quiz_backend.repository.QuizResultRepository;
import com.quiztournament.quiz_backend.repository.TournamentRepository;
import com.quiztournament.quiz_backend.repository.UserRepository;
import com.quiztournament.quiz_backend.repository.UserTournamentScoreRepository;
import com.quiztournament.quiz_backend.service.CustomUserDetailsService.CustomUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing quiz questions for tournaments
 * Handles question fetching, caching, and quiz session management
 */
@Service
public class QuestionService {

    @Autowired
    private OpenTDBService openTDBService;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTournamentScoreRepository userTournamentScoreRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    // In-memory cache for tournament questions
    // Key: tournamentId, Value: List of questions
    private final Map<Long, List<OpenTDBQuestion>> tournamentQuestionsCache = new ConcurrentHashMap<>();

    // User quiz sessions - tracks user progress through questions
    // Key: userId-tournamentId, Value: UserQuizSession
    private final Map<String, UserQuizSession> userQuizSessions = new ConcurrentHashMap<>();

    /**
     * Get questions for a tournament
     * Fetches from cache or OpenTDB API if not cached
     * @param tournamentId Tournament ID
     * @return List of questions for the tournament
     */
    public List<QuestionResponse> getTournamentQuestions(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + tournamentId));

        User currentUser = getCurrentUser();

        // Allow retaking tournaments - previous participation check removed
        // Users can now retake tournaments and their best/latest score will be recorded

        // Check tournament status
        switch (tournament.getStatus()) {
            case UPCOMING:
                throw new RuntimeException("Tournament has not started yet");
            case PAST:
                throw new RuntimeException("Tournament has already ended");
            case ONGOING:
                // Proceed with questions
                break;
        }

        // Get or fetch questions for tournament
        List<OpenTDBQuestion> questions = getOrFetchQuestionsForTournament(tournament);

        // Convert to response format (without correct answers)
        List<QuestionResponse> questionResponses = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            QuestionResponse response = QuestionResponse.fromOpenTDBQuestion(
                    questions.get(i), i + 1, questions.size()
            );
            questionResponses.add(response);
        }

        // Initialize user quiz session
        initializeUserQuizSession(currentUser.getId(), tournamentId, questions);

        return questionResponses;
    }

    /**
     * Get a specific question by number for a user's quiz session
     * @param tournamentId Tournament ID
     * @param questionNumber Question number (1-based)
     * @return Question response
     */
    public QuestionResponse getQuestionByNumber(Long tournamentId, Integer questionNumber) {
        User currentUser = getCurrentUser();
        String sessionKey = getSessionKey(currentUser.getId(), tournamentId);

        UserQuizSession session = userQuizSessions.get(sessionKey);
        if (session == null) {
            throw new RuntimeException("No active quiz session found. Please start the quiz first.");
        }

        if (questionNumber < 1 || questionNumber > session.getQuestions().size()) {
            throw new RuntimeException("Invalid question number: " + questionNumber);
        }

        OpenTDBQuestion question = session.getQuestions().get(questionNumber - 1);
        return QuestionResponse.fromOpenTDBQuestionOrdered(question, questionNumber, session.getQuestions().size());
    }

    /**
     * Validate an answer for a specific question
     * @param tournamentId Tournament ID
     * @param questionNumber Question number (1-based)
     * @param userAnswer User's answer
     * @return Answer validation result
     */
    public AnswerValidationResult validateAnswer(Long tournamentId, Integer questionNumber, String userAnswer) {
        User currentUser = getCurrentUser();
        String sessionKey = getSessionKey(currentUser.getId(), tournamentId);

        UserQuizSession session = userQuizSessions.get(sessionKey);
        if (session == null) {
            throw new RuntimeException("No active quiz session found");
        }

        if (questionNumber < 1 || questionNumber > session.getQuestions().size()) {
            throw new RuntimeException("Invalid question number: " + questionNumber);
        }

        OpenTDBQuestion question = session.getQuestions().get(questionNumber - 1);
        boolean isCorrect = question.isCorrectAnswer(userAnswer);

        // Store answer in session
        session.recordAnswer(questionNumber, userAnswer, isCorrect, question.getCorrectAnswer(), question.getQuestion());

        return new AnswerValidationResult(
                isCorrect,
                question.getCorrectAnswer(),
                userAnswer,
                questionNumber,
                session.getCorrectAnswerCount(),
                session.getQuestions().size()
        );
    }

    /**
     * Get quiz session status for a user
     * @param tournamentId Tournament ID
     * @return Quiz session status
     */
    public QuizSessionStatus getQuizSessionStatus(Long tournamentId) {
        User currentUser = getCurrentUser();
        String sessionKey = getSessionKey(currentUser.getId(), tournamentId);

        UserQuizSession session = userQuizSessions.get(sessionKey);
        if (session == null) {
            return new QuizSessionStatus(false, 0, 0, 0, false);
        }

        return new QuizSessionStatus(
                true,
                session.getCurrentQuestionNumber(),
                session.getCorrectAnswerCount(),
                session.getQuestions().size(),
                session.isCompleted()
        );
    }

    /**
     * Complete quiz and calculate final score
     * @param tournamentId Tournament ID
     * @return Final quiz result
     */
    public QuizCompletionResult completeQuiz(Long tournamentId) {
        User currentUser = getCurrentUser();
        String sessionKey = getSessionKey(currentUser.getId(), tournamentId);

        UserQuizSession session = userQuizSessions.get(sessionKey);
        if (session == null) {
            throw new RuntimeException("No active quiz session found");
        }

        if (!session.isCompleted()) {
            throw new RuntimeException("Quiz is not yet completed. Answer all questions first.");
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Calculate score and pass/fail status
        int correctAnswers = session.getCorrectAnswerCount();
        int totalQuestions = session.getQuestions().size();
        double percentage = (correctAnswers * 100.0) / totalQuestions;
        boolean passed = percentage >= tournament.getMinPassingScore();

        // Calculate time taken (rough estimate based on session duration)
        long sessionStartTime = session.getStartTime();
        int timeTakenSeconds = (int) ((System.currentTimeMillis() - sessionStartTime) / 1000);

        // Save quiz result to database (both new and legacy tables)
        try {
            // Save to new QuizResult table
            Optional<QuizResult> existingResult = quizResultRepository
                .findByUserAndTournament(currentUser, tournament);
            
            if (existingResult.isPresent()) {
                // Update existing result
                QuizResult quizResult = existingResult.get();
                quizResult.setScore(correctAnswers);
                quizResult.setTotalQuestions(totalQuestions);
                quizResult.setPercentage(percentage);
                quizResult.setPassed(passed);
                quizResult.setTimeTakenSeconds(timeTakenSeconds);
                quizResult.setCompletedAt(java.time.LocalDateTime.now());
                quizResultRepository.save(quizResult);
            } else {
                // Create new result
                QuizResult quizResult = new QuizResult(
                    currentUser, 
                    tournament, 
                    correctAnswers, 
                    totalQuestions, 
                    percentage, 
                    passed, 
                    timeTakenSeconds
                );
                quizResultRepository.save(quizResult);
            }

            // Also save to legacy UserTournamentScore table for leaderboard compatibility
            // Convert score to out of 10 format expected by legacy system
            int scoreOutOf10 = (int) Math.round((correctAnswers * 10.0) / totalQuestions);
            
            // Check if user already has a score for this tournament
            Optional<UserTournamentScore> existingScore = userTournamentScoreRepository
                .findByUserAndTournament(currentUser, tournament);
            
            if (existingScore.isPresent()) {
                // Update existing score
                UserTournamentScore legacyScore = existingScore.get();
                legacyScore.setScore(scoreOutOf10);
                legacyScore.setCompletedAt(java.time.LocalDateTime.now());
                userTournamentScoreRepository.save(legacyScore);
            } else {
                // Create new score
                UserTournamentScore legacyScore = new UserTournamentScore(
                    currentUser,
                    tournament,
                    scoreOutOf10
                );
                userTournamentScoreRepository.save(legacyScore);
            }
            
        } catch (Exception e) {
            // Log error but don't fail the quiz completion
            System.err.println("Failed to save quiz result: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for debugging
        }

        // Clean up session
        userQuizSessions.remove(sessionKey);

        return new QuizCompletionResult(
                correctAnswers,
                totalQuestions,
                percentage,
                passed,
                tournament.getMinPassingScore(),
                session.getAnswerHistory()
        );
    }

    /**
     * Get or fetch questions for tournament (with caching)
     */
    private List<OpenTDBQuestion> getOrFetchQuestionsForTournament(Tournament tournament) {
        Long tournamentId = tournament.getId();

        // Check cache first
        if (tournamentQuestionsCache.containsKey(tournamentId)) {
            return tournamentQuestionsCache.get(tournamentId);
        }

        // Fetch from OpenTDB API
        List<OpenTDBQuestion> questions = openTDBService.fetchQuestions(
                tournament.getCategory(),
                tournament.getDifficulty(),
                10 // Always fetch 10 questions per tournament
        );

        // Cache the questions
        tournamentQuestionsCache.put(tournamentId, questions);

        return questions;
    }

    /**
     * Initialize user quiz session
     */
    private void initializeUserQuizSession(Long userId, Long tournamentId, List<OpenTDBQuestion> questions) {
        String sessionKey = getSessionKey(userId, tournamentId);
        UserQuizSession session = new UserQuizSession(userId, tournamentId, questions);
        userQuizSessions.put(sessionKey, session);
    }

    /**
     * Generate session key for user-tournament combination
     */
    private String getSessionKey(Long userId, Long tournamentId) {
        return userId + "-" + tournamentId;
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();

        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    /**
     * Clear cache for a specific tournament (useful when tournament is updated)
     */
    public void clearTournamentCache(Long tournamentId) {
        tournamentQuestionsCache.remove(tournamentId);
    }

    /**
     * Clear all cached questions (useful for testing or memory management)
     */
    public void clearAllCache() {
        tournamentQuestionsCache.clear();
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedTournaments", tournamentQuestionsCache.size());
        stats.put("activeQuizSessions", userQuizSessions.size());
        return stats;
    }

    // Inner classes for session management

    /**
     * Represents a user's quiz session
     */
    private static class UserQuizSession {
        private final Long userId;
        private final Long tournamentId;
        private final List<OpenTDBQuestion> questions;
        private final Map<Integer, UserAnswer> answers;
        private final long startTime;

        public UserQuizSession(Long userId, Long tournamentId, List<OpenTDBQuestion> questions) {
            this.userId = userId;
            this.tournamentId = tournamentId;
            this.questions = questions;
            this.answers = new HashMap<>();
            this.startTime = System.currentTimeMillis();
        }

        public void recordAnswer(int questionNumber, String answer, boolean isCorrect, String correctAnswer, String question) {
            answers.put(questionNumber, new UserAnswer(answer, isCorrect, System.currentTimeMillis(), correctAnswer, question));
        }

        public int getCurrentQuestionNumber() {
            return answers.size() + 1;
        }

        public int getCorrectAnswerCount() {
            return (int) answers.values().stream()
                    .filter(UserAnswer::isCorrect)
                    .count();
        }

        public boolean isCompleted() {
            return answers.size() >= questions.size();
        }

        public List<OpenTDBQuestion> getQuestions() {
            return questions;
        }

        public Map<Integer, UserAnswer> getAnswerHistory() {
            return new HashMap<>(answers);
        }

        public long getStartTime() {
            return startTime;
        }
    }

    /**
     * Represents a user's answer to a question
     */
    public static class UserAnswer {
        private final String answer;
        private final boolean correct;
        private final long timestamp;
        private final String correctAnswer;
        private final String question;

        public UserAnswer(String answer, boolean correct, long timestamp, String correctAnswer, String question) {
            this.answer = answer;
            this.correct = correct;
            this.timestamp = timestamp;
            this.correctAnswer = correctAnswer;
            this.question = question;
        }

        public String getAnswer() { return answer; }
        public boolean isCorrect() { return correct; }
        public long getTimestamp() { return timestamp; }
        public String getCorrectAnswer() { return correctAnswer; }
        public String getQuestion() { return question; }
    }

    /**
     * Result of answer validation
     */
    public static class AnswerValidationResult {
        private final boolean correct;
        private final String correctAnswer;
        private final String userAnswer;
        private final int questionNumber;
        private final int correctCount;
        private final int totalQuestions;

        public AnswerValidationResult(boolean correct, String correctAnswer, String userAnswer,
                                      int questionNumber, int correctCount, int totalQuestions) {
            this.correct = correct;
            this.correctAnswer = correctAnswer;
            this.userAnswer = userAnswer;
            this.questionNumber = questionNumber;
            this.correctCount = correctCount;
            this.totalQuestions = totalQuestions;
        }

        // Getters
        public boolean isCorrect() { return correct; }
        public String getCorrectAnswer() { return correctAnswer; }
        public String getUserAnswer() { return userAnswer; }
        public int getQuestionNumber() { return questionNumber; }
        public int getCorrectCount() { return correctCount; }
        public int getTotalQuestions() { return totalQuestions; }
    }

    /**
     * Quiz session status
     */
    public static class QuizSessionStatus {
        private final boolean active;
        private final int currentQuestion;
        private final int correctAnswers;
        private final int totalQuestions;
        private final boolean completed;

        public QuizSessionStatus(boolean active, int currentQuestion, int correctAnswers,
                                 int totalQuestions, boolean completed) {
            this.active = active;
            this.currentQuestion = currentQuestion;
            this.correctAnswers = correctAnswers;
            this.totalQuestions = totalQuestions;
            this.completed = completed;
        }

        // Getters
        public boolean isActive() { return active; }
        public int getCurrentQuestion() { return currentQuestion; }
        public int getCorrectAnswers() { return correctAnswers; }
        public int getTotalQuestions() { return totalQuestions; }
        public boolean isCompleted() { return completed; }
    }

    /**
     * Quiz completion result
     */
    public static class QuizCompletionResult {
        private final int correctAnswers;
        private final int totalQuestions;
        private final double percentage;
        private final boolean passed;
        private final double minPassingScore;
        private final Map<Integer, UserAnswer> answerHistory;

        public QuizCompletionResult(int correctAnswers, int totalQuestions, double percentage,
                                    boolean passed, double minPassingScore,
                                    Map<Integer, UserAnswer> answerHistory) {
            this.correctAnswers = correctAnswers;
            this.totalQuestions = totalQuestions;
            this.percentage = percentage;
            this.passed = passed;
            this.minPassingScore = minPassingScore;
            this.answerHistory = answerHistory;
        }

        // Getters
        public int getCorrectAnswers() { return correctAnswers; }
        public int getTotalQuestions() { return totalQuestions; }
        public double getPercentage() { return percentage; }
        public boolean isPassed() { return passed; }
        public double getMinPassingScore() { return minPassingScore; }
        public Map<Integer, UserAnswer> getAnswerHistory() { return answerHistory; }
    }
}