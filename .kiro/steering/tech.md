# Technology Stack & Build System

## Backend Stack
- **Framework**: Spring Boot 3.1.5 with Java 17+
- **Security**: Spring Security with JWT authentication (JJWT 0.11.5)
- **Database**: H2 (development) / PostgreSQL (production) with Spring Data JPA
- **Build Tool**: Maven 3.6+
- **Testing**: JUnit 5, Mockito, Testcontainers, WireMock
- **Documentation**: Swagger/OpenAPI (SpringDoc 2.2.0)
- **External APIs**: OpenTDB (Open Trivia Database) integration

## Frontend Stack
- **Framework**: React 18 with functional components and hooks
- **Routing**: React Router 6
- **HTTP Client**: Axios for API communication
- **State Management**: React Context API
- **Styling**: CSS3 with responsive design patterns
- **Build Tool**: Create React App (react-scripts 5.0.1)

## Development Environment
- **Java Version**: 17+ (tested with OpenJDK 24)
- **Node.js**: 14+ (tested with Node 18)
- **Database**: H2 in-memory for development, PostgreSQL for production
- **Containerization**: Docker with docker-compose for production deployment

## Common Commands

### Backend Development
```bash
cd backend
mvn spring-boot:run          # Start development server (port 8080)
mvn test                     # Run unit tests
mvn verify                   # Run integration tests
mvn jacoco:report           # Generate coverage report
mvn clean install          # Full build with tests
```

### Frontend Development
```bash
cd frontend
npm install                 # Install dependencies
npm start                  # Start development server (port 3000)
npm test                   # Run Jest tests
npm run build             # Production build
```

### Quick Development Start
```bash
start-dev.bat             # Windows batch script to start both services
```

### Docker Deployment
```bash
docker-compose up --build  # Build and start all services
docker-compose down       # Stop all services
```

## Configuration Files
- **Backend**: `application.properties`, `application-prod.properties`
- **Frontend**: `package.json`, `.env` files for environment variables
- **Docker**: `docker-compose.yml`, `Dockerfile.backend`, `Dockerfile.frontend`
- **Build**: `pom.xml` (Maven), `package.json` (npm)

## Testing Strategy (Assessment Requirements)
- **Unit Tests**: JUnit 5 with Mockito for backend, Jest for frontend
- **Integration Tests**: Testcontainers for database testing, WireMock for external API mocking
- **Coverage**: JaCoCo with 80% minimum coverage requirement
- **Functional Testing**: Must cover tournament CRUD operations and question viewing
- **UI Testing**: Form validation, modal interactions, table operations
- **E2E Testing**: Manual testing with Swagger UI and browser testing

## Assessment-Specific Requirements
- **Frontend-Backend Decoupling**: Separate applications communicating via REST APIs
- **Dependency Management**: Proper Maven (backend) and npm (frontend) configuration
- **Git Repository**: Separate frontend repository with meaningful commit messages
- **Documentation**: Comprehensive README with screenshots and setup instructions
- **Code Quality**: Design patterns, best practices, elegant code structure