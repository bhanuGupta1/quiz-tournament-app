package com.quiztournament.quiz_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for Swagger/OpenAPI documentation
 * Provides comprehensive API documentation for the Quiz Tournament system
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.backend.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quiz Tournament API")
                        .description("Online Quiz Tournament System - Backend REST API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Quiz Tournament Team")
                                .email("support@quiztournament.com")
                                .url("https://github.com/quiz-tournament"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.quiztournament.com")
                                .description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token Authentication")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}