package com.quiztournament.quiz_backend.config;

import com.quiztournament.quiz_backend.service.AuthService;
import com.quiztournament.quiz_backend.service.TournamentService;
import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/**
 * Data initialization component
 * Creates default admin user and sample data on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private TournamentService tournamentService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Add a small delay to ensure database is fully initialized
            Thread.sleep(1000);
            
            // Create default admin user
            authService.createDefaultAdmin();
            
            // Create sample tournaments for demonstration
            createSampleTournaments();

            System.out.println("=== Quiz Tournament Application Started ===");
            System.out.println("Default Admin Credentials:");
            System.out.println("Username: admin");
            System.out.println("Password: op@1234");
            System.out.println("Sample tournaments created for testing");
            System.out.println("==========================================");
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            // Don't fail the application startup, just log the error
            e.printStackTrace();
        }
    }
    
    private void createSampleTournaments() {
        try {
            // Check if tournaments already exist
            if (tournamentService.getTotalTournamentCount() > 0) {
                System.out.println("Tournaments already exist, skipping sample data creation");
                return;
            }
            
            // Create sample tournaments with different categories and difficulties
            createSampleTournament("Science Quiz Challenge", "Science", "easy", 
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), 60.0);
                
            createSampleTournament("History Masters", "History", "medium", 
                LocalDate.now(), LocalDate.now().plusDays(14), 70.0);
                
            createSampleTournament("Sports Trivia Championship", "Sports", "hard", 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(21), 80.0);
                
            createSampleTournament("General Knowledge Quiz", "General Knowledge", "easy", 
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(7), 50.0);
                
            createSampleTournament("Technology Quiz", "Technology", "medium", 
                LocalDate.now(), LocalDate.now().plusDays(10), 65.0);
                
            System.out.println("Sample tournaments created successfully");
        } catch (Exception e) {
            System.err.println("Error creating sample tournaments: " + e.getMessage());
        }
    }
    
    private void createSampleTournament(String name, String category, String difficulty, 
                                      LocalDate startDate, LocalDate endDate, Double minScore) {
        try {
            TournamentCreateRequest request = new TournamentCreateRequest();
            request.setName(name);
            request.setCategory(category);
            request.setDifficulty(difficulty);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setMinPassingScore(minScore);
            
            tournamentService.createTournament(request);
        } catch (Exception e) {
            System.err.println("Failed to create tournament: " + name + " - " + e.getMessage());
        }
    }
}