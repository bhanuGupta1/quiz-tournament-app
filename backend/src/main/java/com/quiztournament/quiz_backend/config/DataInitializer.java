package com.quiztournament.quiz_backend.config;

import com.quiztournament.quiz_backend.service.AuthService;
import com.quiztournament.quiz_backend.service.TournamentService;
import com.quiztournament.quiz_backend.service.CustomUserDetailsService;
import com.quiztournament.quiz_backend.dto.TournamentCreateRequest;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Add a small delay to ensure database is fully initialized
            Thread.sleep(1000);
            
            // Create default admin user
            authService.createDefaultAdmin();
            
            // Create simple test users for easy testing
            authService.createTestUsers();
            
            // Create sample tournaments for demonstration
            createSampleTournaments();

            System.out.println("=== Quiz Tournament Application Started ===");
            System.out.println("🔑 Test Login Credentials:");
            System.out.println("👤 Admin: username=admin, password=op@1234");
            System.out.println("👤 Player: username=user, password=user");
            System.out.println("👤 Player1: username=player1, password=password");
            System.out.println("👤 Player2: username=player2, password=password");
            System.out.println("");
            System.out.println("✅ Sample tournaments created for testing");
            System.out.println("🌐 Frontend: http://localhost:3000");
            System.out.println("🔧 Backend API: http://localhost:8080");
            System.out.println("📚 Swagger UI: http://localhost:8080/swagger-ui.html");
            System.out.println("==========================================");
        } catch (Exception e) {
            System.err.println("Error during data initialization: " + e.getMessage());
            // Don't fail the application startup, just log the error
            e.printStackTrace();
        }
    }
    
    private void createSampleTournaments() {
        // Always skip sample tournament creation to preserve existing data
        System.out.println("Skipping sample tournament creation to preserve existing data");
    }
    
    private void setupAdminSecurityContext() {
        try {
            // Find the admin user
            User adminUser = userRepository.findByUsername("admin")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
            
            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername("admin");
            
            // Create authentication token
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // Set in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            System.out.println("Admin security context set up for tournament creation");
        } catch (Exception e) {
            System.err.println("Failed to set up admin security context: " + e.getMessage());
            throw e;
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