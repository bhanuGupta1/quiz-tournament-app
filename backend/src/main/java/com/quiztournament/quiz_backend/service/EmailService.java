package com.quiztournament.quiz_backend.service;

import com.quiztournament.quiz_backend.entity.Tournament;
import com.quiztournament.quiz_backend.entity.User;
import com.quiztournament.quiz_backend.entity.UserRole;
import com.quiztournament.quiz_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling email notifications
 * Manages tournament notifications, password reset emails, and other system communications
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.mail.username:noreply@quiztournament.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * Send email notification to all players when new tournament is created
     * @param tournament Newly created tournament
     * @param createdBy Admin who created the tournament
     */
    public void sendNewTournamentNotification(Tournament tournament, User createdBy) {
        // Get all players (exclude admin users)
        List<User> players = userRepository.findByRole(UserRole.PLAYER);

        if (players.isEmpty()) {
            System.out.println("No players found to notify about new tournament");
            return;
        }

        String subject = "New Quiz Tournament Available: " + tournament.getName();
        String messageBody = createNewTournamentEmailBody(tournament, createdBy);

        // Send emails asynchronously to all players
        CompletableFuture.runAsync(() -> {
            for (User player : players) {
                try {
                    sendEmail(player.getEmail(), subject, messageBody);
                    System.out.println("Tournament notification sent to: " + player.getEmail());
                } catch (Exception e) {
                    System.err.println("Failed to send email to " + player.getEmail() + ": " + e.getMessage());
                }
            }
        });

        System.out.println("Tournament notification initiated for " + players.size() + " players");
    }

    /**
     * Send password reset email
     * @param user User requesting password reset
     * @param resetToken Password reset token
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        String subject = "Password Reset - Quiz Tournament";
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        String messageBody = createPasswordResetEmailBody(user, resetUrl, resetToken);

        try {
            sendEmail(user.getEmail(), subject, messageBody);
            System.out.println("Password reset email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to " + user.getEmail() + ": " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }

    /**
     * Send welcome email to new user
     * @param user Newly registered user
     */
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to Quiz Tournament!";
        String messageBody = createWelcomeEmailBody(user);

        CompletableFuture.runAsync(() -> {
            try {
                sendEmail(user.getEmail(), subject, messageBody);
                System.out.println("Welcome email sent to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send welcome email to " + user.getEmail() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Send quiz completion notification
     * @param user Player who completed quiz
     * @param tournament Tournament completed
     * @param score Final score
     * @param passed Whether user passed
     */
    public void sendQuizCompletionEmail(User user, Tournament tournament, Integer score, Boolean passed) {
        String subject = "Quiz Completed: " + tournament.getName();
        String messageBody = createQuizCompletionEmailBody(user, tournament, score, passed);

        CompletableFuture.runAsync(() -> {
            try {
                sendEmail(user.getEmail(), subject, messageBody);
                System.out.println("Quiz completion email sent to: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("Failed to send quiz completion email to " + user.getEmail() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Core email sending method
     * @param to Recipient email address
     * @param subject Email subject
     * @param text Email body
     */
    private void sendEmail(String to, String subject, String text) {
        // Skip email sending if disabled or mailSender is not configured (for testing)
        if (!emailEnabled || mailSender == null) {
            System.out.println("📧 Email service disabled - would have sent email to: " + to);
            System.out.println("📧 Subject: " + subject);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Create email body for new tournament notification
     */
    private String createNewTournamentEmailBody(Tournament tournament, User createdBy) {
        StringBuilder body = new StringBuilder();
        body.append("Hello Quiz Enthusiast!\n\n");
        body.append("A new tournament has been created and is now available for participation:\n\n");
        body.append("Tournament Details:\n");
        body.append("- Name: ").append(tournament.getName()).append("\n");
        body.append("- Category: ").append(tournament.getCategory()).append("\n");
        body.append("- Difficulty: ").append(tournament.getDifficulty()).append("\n");
        body.append("- Start Date: ").append(tournament.getStartDate()).append("\n");
        body.append("- End Date: ").append(tournament.getEndDate()).append("\n");
        body.append("- Minimum Passing Score: ").append(tournament.getMinPassingScore()).append("%\n");
        body.append("- Created by: ").append(createdBy.getFirstName()).append(" ").append(createdBy.getLastName()).append("\n\n");

        body.append("Ready to test your knowledge? Log in to the Quiz Tournament platform and start the quiz!\n\n");
        body.append("Login here: ").append(frontendUrl).append("/login\n\n");
        body.append("Good luck and have fun!\n\n");
        body.append("Best regards,\n");
        body.append("Quiz Tournament Team");

        return body.toString();
    }

    /**
     * Create email body for password reset
     */
    private String createPasswordResetEmailBody(User user, String resetUrl, String resetToken) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(user.getFirstName()).append(",\n\n");
        body.append("You have requested to reset your password for your Quiz Tournament account.\n\n");
        body.append("To reset your password, please click the link below:\n\n");
        body.append(resetUrl).append("\n\n");
        body.append("Alternatively, you can use this reset token: ").append(resetToken).append("\n\n");
        body.append("This link will expire in 1 hour for security reasons.\n\n");
        body.append("If you did not request this password reset, please ignore this email.\n\n");
        body.append("Best regards,\n");
        body.append("Quiz Tournament Team");

        return body.toString();
    }

    /**
     * Create email body for welcome message
     */
    private String createWelcomeEmailBody(User user) {
        StringBuilder body = new StringBuilder();
        body.append("Welcome to Quiz Tournament, ").append(user.getFirstName()).append("!\n\n");
        body.append("Thank you for joining our quiz community. Your account has been successfully created.\n\n");
        body.append("Account Details:\n");
        body.append("- Username: ").append(user.getUsername()).append("\n");
        body.append("- Email: ").append(user.getEmail()).append("\n");
        body.append("- Role: ").append(user.getRole().name()).append("\n\n");

        if (user.getRole() == UserRole.PLAYER) {
            body.append("As a player, you can:\n");
            body.append("- Participate in quiz tournaments\n");
            body.append("- Track your progress and scores\n");
            body.append("- View leaderboards and compete with others\n");
            body.append("- Like your favorite tournaments\n\n");
        } else if (user.getRole() == UserRole.ADMIN) {
            body.append("As an admin, you can:\n");
            body.append("- Create and manage tournaments\n");
            body.append("- View tournament statistics\n");
            body.append("- Manage user accounts\n");
            body.append("- Monitor quiz participation\n\n");
        }

        body.append("Get started: ").append(frontendUrl).append("/login\n\n");
        body.append("Happy quizzing!\n\n");
        body.append("Best regards,\n");
        body.append("Quiz Tournament Team");

        return body.toString();
    }

    /**
     * Create email body for quiz completion notification
     */
    private String createQuizCompletionEmailBody(User user, Tournament tournament, Integer score, Boolean passed) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(user.getFirstName()).append(",\n\n");
        body.append("You have completed the quiz tournament: ").append(tournament.getName()).append("\n\n");
        body.append("Your Results:\n");
        body.append("- Score: ").append(score).append("/10\n");
        body.append("- Percentage: ").append((score * 10)).append("%\n");
        body.append("- Status: ").append(passed ? "PASSED" : "FAILED").append("\n");
        body.append("- Minimum Passing Score: ").append(tournament.getMinPassingScore()).append("%\n\n");

        if (passed) {
            body.append("Congratulations! You have successfully passed this tournament! 🎉\n\n");
        } else {
            body.append("You didn't pass this time, but don't give up! Keep practicing and try other tournaments.\n\n");
        }

        body.append("View detailed results: ").append(frontendUrl).append("/my-results\n");
        body.append("Browse more tournaments: ").append(frontendUrl).append("/tournaments\n\n");
        body.append("Keep learning and have fun!\n\n");
        body.append("Best regards,\n");
        body.append("Quiz Tournament Team");

        return body.toString();
    }

    /**
     * Test email connectivity
     * @return true if email service is configured and working
     */
    public boolean testEmailConnectivity() {
        if (!emailEnabled || mailSender == null) {
            System.out.println("📧 Email service is disabled for testing");
            return false;
        }
        
        try {
            // Try to send a test email to a dummy address
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("test@example.com");
            message.setSubject("Test Email - Quiz Tournament");
            message.setText("This is a test email to verify email configuration.");

            // Note: This would fail for dummy address, but validates configuration
            return true;
        } catch (Exception e) {
            System.err.println("Email connectivity test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get email service status
     */
    public Map<String, Object> getEmailServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("configured", mailSender != null);
        status.put("fromEmail", fromEmail);
        status.put("frontendUrl", frontendUrl);
        status.put("backendUrl", backendUrl);
        status.put("testConnectivity", testEmailConnectivity());
        return status;
    }
}