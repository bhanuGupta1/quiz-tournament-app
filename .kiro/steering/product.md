# Quiz Tournament Application

A decoupled full-stack quiz tournament platform that enables online quiz competitions with role-based access control. The application serves as a digital replacement for traditional pub quiz nights during lockdowns, allowing users to participate in tournaments with questions sourced from the Open Trivia Database (OpenTDB).

## Assessment Context

This is an academic project demonstrating **frontend-backend decoupling** with separate React frontend and Spring Boot backend applications that communicate via REST APIs. The project emphasizes best practices, code elegance, robustness, and proper documentation.

## Core Features

### Admin Requirements (Assessment Criteria)
- **Tournament CRUD Operations**: Create, update, delete tournaments via modal forms
- **Form Validation**: Graceful error handling for invalid inputs (e.g., blank creator field)
- **Tournament Table View**: Display creator, name, category, difficulty in tabular format
- **Question Management**: View tournament questions, correct/incorrect answers on name click
- **Delete Confirmation**: User prompts before tournament deletion

### Player Requirements (Assessment Criteria)
- **Tournament Browser**: View and select available tournaments
- **Quiz Gameplay**: Interactive quiz participation with real-time feedback
- **Progress Tracking**: Monitor completion status and performance metrics
- **Score Management**: Track individual results for prize allocation

### Technical Requirements
- **Responsive UI Design**: Visually attractive and mobile-friendly interfaces
- **Role-based Authentication**: Secure JWT-based access control
- **API Integration**: RESTful communication between decoupled services
- **Error Handling**: Robust validation and graceful error management

## User Types

- **Admin Users**: Full tournament CRUD operations, question management, system oversight
- **Player Users**: Tournament participation, performance tracking, score viewing

## Business Context

Created for a local pub unable to host weekly quiz tournaments due to nation-wide lockdown. Enables online quiz competitions with score tracking for prize allocation, maintaining the social and competitive aspects of traditional pub quiz nights.