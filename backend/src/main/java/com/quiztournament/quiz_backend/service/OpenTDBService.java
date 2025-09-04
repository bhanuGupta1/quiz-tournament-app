package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.dto.OpenTDBResponse;
import com.quiztournament.quiz_backend.dto.OpenTDBQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for integrating with OpenTDB (Open Trivia Database) API
 * Handles fetching questions from external API with error handling and fallback
 */
@Service
public class OpenTDBService {

    private static final String OPENTDB_BASE_URL = "https://opentdb.com/api.php";
    private static final int DEFAULT_QUESTION_COUNT = 10;
    private static final int MAX_RETRIES = 3;

    @Autowired
    private RestTemplate restTemplate;

    // Category mapping from our system to OpenTDB categories
    private static final Map<String, String> CATEGORY_MAPPING = new HashMap<>();
    static {
        CATEGORY_MAPPING.put("science", "17"); // Science & Nature
        CATEGORY_MAPPING.put("history", "23"); // History
        CATEGORY_MAPPING.put("sports", "21"); // Sports
        CATEGORY_MAPPING.put("geography", "22"); // Geography
        CATEGORY_MAPPING.put("entertainment", "11"); // Entertainment: Film
        CATEGORY_MAPPING.put("general", "9"); // General Knowledge
        CATEGORY_MAPPING.put("mathematics", "19"); // Science: Mathematics
        CATEGORY_MAPPING.put("computer", "18"); // Science: Computers
        CATEGORY_MAPPING.put("music", "12"); // Entertainment: Music
        CATEGORY_MAPPING.put("literature", "10"); // Entertainment: Books
    }

    /**
     * Fetch questions from OpenTDB API for a tournament
     * @param category Tournament category
     * @param difficulty Tournament difficulty
     * @param amount Number of questions to fetch (default 10)
     * @return List of questions from OpenTDB
     */
    public List<OpenTDBQuestion> fetchQuestions(String category, String difficulty, int amount) {
        try {
            String url = buildApiUrl(category, difficulty, amount);

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    OpenTDBResponse response = restTemplate.getForObject(url, OpenTDBResponse.class);

                    if (response != null && response.isSuccessful() && response.getResults() != null) {
                        List<OpenTDBQuestion> questions = response.getResults();

                        // Validate we have enough questions
                        if (questions.size() >= amount) {
                            return questions.subList(0, amount);
                        } else if (questions.size() > 0) {
                            // Fill remaining questions with general knowledge
                            return fillWithGeneralQuestions(questions, amount, difficulty);
                        }
                    } else if (response != null) {
                        System.err.println("OpenTDB API Error: " + response.getResponseMessage());

                        // If not enough questions for specific category, try general knowledge
                        if (response.getResponseCode() == 1 && !isGeneralCategory(category)) {
                            return fetchQuestionsWithFallback("general", difficulty, amount);
                        }
                    }

                } catch (HttpClientErrorException | HttpServerErrorException e) {
                    System.err.println("HTTP error on attempt " + attempt + ": " + e.getMessage());
                    if (attempt == MAX_RETRIES) {
                        return getFallbackQuestions(category, difficulty, amount);
                    }
                    Thread.sleep(1000 * attempt); // Exponential backoff

                } catch (ResourceAccessException e) {
                    System.err.println("Network error on attempt " + attempt + ": " + e.getMessage());
                    if (attempt == MAX_RETRIES) {
                        return getFallbackQuestions(category, difficulty, amount);
                    }
                    Thread.sleep(1000 * attempt);
                }
            }

            // If all attempts failed, return fallback questions
            return getFallbackQuestions(category, difficulty, amount);

        } catch (Exception e) {
            System.err.println("Unexpected error fetching questions: " + e.getMessage());
            return getFallbackQuestions(category, difficulty, amount);
        }
    }

    /**
     * Fetch questions with fallback to general knowledge
     */
    private List<OpenTDBQuestion> fetchQuestionsWithFallback(String category, String difficulty, int amount) {
        try {
            return fetchQuestions(category, difficulty, amount);
        } catch (Exception e) {
            return getFallbackQuestions(category, difficulty, amount);
        }
    }

    /**
     * Fill remaining questions with general knowledge questions
     */
    private List<OpenTDBQuestion> fillWithGeneralQuestions(List<OpenTDBQuestion> existingQuestions,
                                                           int targetAmount, String difficulty) {
        if (existingQuestions.size() >= targetAmount) {
            return existingQuestions;
        }

        int needed = targetAmount - existingQuestions.size();
        List<OpenTDBQuestion> generalQuestions = fetchQuestions("general", difficulty, needed);

        List<OpenTDBQuestion> combined = new ArrayList<>(existingQuestions);
        combined.addAll(generalQuestions);

        return combined.subList(0, Math.min(combined.size(), targetAmount));
    }

    /**
     * Build the API URL with parameters
     */
    private String buildApiUrl(String category, String difficulty, int amount) {
        StringBuilder url = new StringBuilder(OPENTDB_BASE_URL);
        url.append("?amount=").append(amount);

        // Add category if mapped
        String categoryId = CATEGORY_MAPPING.get(category.toLowerCase());
        if (categoryId != null) {
            url.append("&category=").append(categoryId);
        }

        // Add difficulty
        if (difficulty != null && !difficulty.isEmpty()) {
            url.append("&difficulty=").append(difficulty.toLowerCase());
        }

        // Mixed question types (both multiple choice and true/false)
        // Don't specify type to get both

        System.out.println("OpenTDB API URL: " + url.toString());
        return url.toString();
    }

    /**
     * Check if category is general knowledge
     */
    private boolean isGeneralCategory(String category) {
        return "general".equalsIgnoreCase(category);
    }

    /**
     * Get fallback questions when API is unavailable
     * These are hardcoded questions to ensure the system works even without external API
     */
    private List<OpenTDBQuestion> getFallbackQuestions(String category, String difficulty, int amount) {
        System.out.println("Using fallback questions for category: " + category);

        List<OpenTDBQuestion> fallbackQuestions = new ArrayList<>();

        // Add some basic questions based on category
        if ("science".equalsIgnoreCase(category)) {
            fallbackQuestions.addAll(getScienceFallbackQuestions());
        } else if ("history".equalsIgnoreCase(category)) {
            fallbackQuestions.addAll(getHistoryFallbackQuestions());
        } else if ("sports".equalsIgnoreCase(category)) {
            fallbackQuestions.addAll(getSportsFallbackQuestions());
        } else {
            fallbackQuestions.addAll(getGeneralFallbackQuestions());
        }

        // Adjust difficulty if specified
        adjustDifficultyForFallback(fallbackQuestions, difficulty);

        // Return requested amount
        return fallbackQuestions.subList(0, Math.min(fallbackQuestions.size(), amount));
    }

    /**
     * Science fallback questions
     */
    private List<OpenTDBQuestion> getScienceFallbackQuestions() {
        List<OpenTDBQuestion> questions = new ArrayList<>();

        questions.add(new OpenTDBQuestion("Science & Nature", "multiple", "medium",
                "What is the chemical symbol for gold?",
                "Au", List.of("Ag", "Go", "Gd")));

        questions.add(new OpenTDBQuestion("Science & Nature", "boolean", "easy",
                "The Earth is the third planet from the Sun.",
                "True", List.of("False")));

        questions.add(new OpenTDBQuestion("Science & Nature", "multiple", "hard",
                "What is the most abundant gas in Earth's atmosphere?",
                "Nitrogen", List.of("Oxygen", "Carbon Dioxide", "Argon")));

        return questions;
    }

    /**
     * History fallback questions
     */
    private List<OpenTDBQuestion> getHistoryFallbackQuestions() {
        List<OpenTDBQuestion> questions = new ArrayList<>();

        questions.add(new OpenTDBQuestion("History", "multiple", "medium",
                "In which year did World War II end?",
                "1945", List.of("1944", "1946", "1943")));

        questions.add(new OpenTDBQuestion("History", "boolean", "easy",
                "The Great Wall of China was built in a single dynasty.",
                "False", List.of("True")));

        return questions;
    }

    /**
     * Sports fallback questions
     */
    private List<OpenTDBQuestion> getSportsFallbackQuestions() {
        List<OpenTDBQuestion> questions = new ArrayList<>();

        questions.add(new OpenTDBQuestion("Sports", "multiple", "medium",
                "How many players are there in a basketball team on court?",
                "5", List.of("6", "7", "4")));

        questions.add(new OpenTDBQuestion("Sports", "boolean", "easy",
                "A soccer match consists of two halves.",
                "True", List.of("False")));

        return questions;
    }

    /**
     * General knowledge fallback questions
     */
    private List<OpenTDBQuestion> getGeneralFallbackQuestions() {
        List<OpenTDBQuestion> questions = new ArrayList<>();

        questions.add(new OpenTDBQuestion("General Knowledge", "multiple", "medium",
                "What is the capital of Australia?",
                "Canberra", List.of("Sydney", "Melbourne", "Perth")));

        questions.add(new OpenTDBQuestion("General Knowledge", "boolean", "easy",
                "There are 7 continents on Earth.",
                "True", List.of("False")));

        questions.add(new OpenTDBQuestion("General Knowledge", "multiple", "hard",
                "Which planet has the most moons?",
                "Jupiter", List.of("Saturn", "Neptune", "Uranus")));

        return questions;
    }

    /**
     * Adjust difficulty for fallback questions
     */
    private void adjustDifficultyForFallback(List<OpenTDBQuestion> questions, String targetDifficulty) {
        if (targetDifficulty != null) {
            questions.forEach(q -> q.setDifficulty(targetDifficulty));
        }
    }

    /**
     * Get available categories for frontend
     */
    public Map<String, String> getAvailableCategories() {
        Map<String, String> categories = new HashMap<>();
        categories.put("general", "General Knowledge");
        categories.put("science", "Science & Nature");
        categories.put("history", "History");
        categories.put("sports", "Sports");
        categories.put("geography", "Geography");
        categories.put("entertainment", "Entertainment");
        categories.put("mathematics", "Mathematics");
        categories.put("computer", "Computer Science");
        categories.put("music", "Music");
        categories.put("literature", "Literature");
        return categories;
    }

    /**
     * Test OpenTDB API connectivity
     */
    public boolean testApiConnectivity() {
        try {
            String url = OPENTDB_BASE_URL + "?amount=1";
            OpenTDBResponse response = restTemplate.getForObject(url, OpenTDBResponse.class);
            return response != null && response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}