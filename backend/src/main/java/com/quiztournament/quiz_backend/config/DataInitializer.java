package com.quiztournament.quiz_backend.config;

import com.quiztournament.quiz_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data initialization component
 * Creates default admin user and sample data on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AuthService authService;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user
        authService.createDefaultAdmin();

        System.out.println("=== Quiz Tournament Application Started ===");
        System.out.println("Default Admin Credentials:");
        System.out.println("Username: admin");
        System.out.println("Password: op@1234");
        System.out.println("==========================================");
    }
}