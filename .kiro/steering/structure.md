# Project Structure & Organization

## Root Directory Layout
```
quiz-tournament-app/
├── backend/                 # Spring Boot backend application
├── frontend/                # React frontend application
├── .kiro/                   # Kiro IDE configuration and steering
├── docker-compose.yml       # Production deployment configuration
├── start-dev.bat           # Development environment startup script
└── README.md               # Project documentation
```

## Backend Structure (`backend/`)
```
backend/
├── src/main/java/com/quiztournament/quiz_backend/
│   ├── config/             # Configuration classes (Security, JWT, Swagger, etc.)
│   ├── controller/         # REST API controllers
│   ├── dto/               # Data Transfer Objects for API requests/responses
│   ├── entity/            # JPA entities (database models)
│   ├── repository/        # Spring Data JPA repositories
│   ├── service/           # Business logic layer
│   ├── util/              # Utility classes (JWT, Validation)
│   └── QuizBackendApplication.java  # Main Spring Boot application
├── src/main/resources/
│   ├── application.properties       # Development configuration
│   ├── application-prod.properties  # Production configuration
│   └── quiz_results_table.sql      # Database initialization
├── src/test/               # Test classes mirroring main structure
└── pom.xml                # Maven dependencies and build configuration
```

## Frontend Structure (`frontend/`)
```
frontend/
├── public/                 # Static assets and index.html
├── src/
│   ├── components/         # Reusable React components
│   │   ├── Header.js       # Navigation header component
│   │   ├── ErrorBoundary.js # Error handling wrapper
│   │   └── HealthCheck.js  # System health monitoring
│   ├── context/           # React Context providers
│   │   └── AuthContext.js  # Authentication state management
│   ├── pages/             # Page-level components (routes)
│   │   ├── Home.js         # Landing page
│   │   ├── Login.js        # User authentication
│   │   ├── Register.js     # User registration
│   │   ├── Dashboard.js    # Player dashboard
│   │   ├── AdminDashboard.js # Admin control panel
│   │   ├── CreateTournament.js # Tournament creation form
│   │   ├── ManageTournaments.js # Tournament management
│   │   ├── Quiz.js         # Quiz gameplay interface
│   │   └── [other pages]   # Additional page components
│   ├── services/          # API communication layer
│   │   ├── api.js          # Backend API client
│   │   └── opentdb.js      # OpenTDB API integration
│   ├── utils/             # Utility functions
│   │   ├── logger.js       # Logging utilities
│   │   └── validation.js   # Form validation helpers
│   ├── App.js             # Main application component
│   ├── App.css            # Global styles
│   └── index.js           # React application entry point
├── package.json           # npm dependencies and scripts
└── package-lock.json      # Dependency lock file
```

## Architecture Patterns

### Backend Patterns
- **Layered Architecture**: Controller → Service → Repository → Entity
- **DTO Pattern**: Separate request/response objects from entities
- **Repository Pattern**: Spring Data JPA repositories for data access
- **Dependency Injection**: Spring's IoC container for component management
- **Configuration Classes**: Centralized configuration in `config/` package

### Frontend Patterns
- **Component-Based Architecture**: Reusable React functional components
- **Context Pattern**: Global state management with React Context
- **Service Layer**: Centralized API communication in `services/`
- **Page-Component Separation**: Route-level components in `pages/`, reusable UI in `components/`
- **Custom Hooks**: Reusable stateful logic extraction

## Naming Conventions

### Backend (Java)
- **Classes**: PascalCase (e.g., `TournamentController`, `UserService`)
- **Methods**: camelCase (e.g., `createTournament`, `findByUsername`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `JWT_SECRET`, `DEFAULT_TIMEOUT`)
- **Packages**: lowercase with dots (e.g., `com.quiztournament.quiz_backend.service`)

### Frontend (JavaScript/React)
- **Components**: PascalCase (e.g., `TournamentDetails`, `CreateTournament`)
- **Files**: PascalCase for components, camelCase for utilities
- **Functions**: camelCase (e.g., `handleSubmit`, `fetchTournaments`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `API_BASE_URL`, `QUIZ_TIMER`)

## File Organization Rules
- **Single Responsibility**: One class/component per file
- **Logical Grouping**: Related functionality grouped in same package/directory
- **Clear Separation**: Distinct layers (controller, service, repository) in separate packages
- **Configuration Centralization**: All config classes in `config/` package
- **Test Mirroring**: Test structure mirrors main source structure

## Assessment-Specific UI Components

### Required Admin UI Components
- **Tournament Modal Forms**: Create/Update tournament with validation
- **Tournament Table**: Sortable table with creator, name, category, difficulty columns
- **Delete Confirmation Dialog**: User confirmation before deletion
- **Question Viewer**: Display questions and answers when tournament name clicked
- **Validation Messages**: Clear error feedback for form validation failures

### Required Player UI Components
- **Tournament Browser**: Grid/list view of available tournaments
- **Quiz Interface**: Question display with multiple choice options
- **Progress Tracker**: Visual indication of quiz completion status
- **Results Display**: Score and performance metrics after completion

### UI Design Principles
- **Responsive Design**: Mobile-first approach with breakpoints
- **Modal Patterns**: Consistent modal usage for forms and confirmations
- **Table Interactions**: Clickable rows, sorting, and filtering capabilities
- **Form Validation**: Real-time validation with clear error messaging
- **Loading States**: Visual feedback during API operations