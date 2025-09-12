# 🏆 Quiz Tournament Application - Phase 8 Complete

A comprehensive full-stack quiz tournament platform built with Spring Boot backend and React frontend, featuring OpenTDB API integration for dynamic quiz questions and real-time gameplay.

## 🎯 Phase 8 Achievements

✅ **Complete Frontend Implementation**  
✅ **OpenTDB API Integration**  
✅ **Role-Based Authentication System**  
✅ **Real-Time Quiz Gameplay**  
✅ **Admin Tournament Management**  
✅ **Player Dashboard & Statistics**  
✅ **Comprehensive Error Handling**  
✅ **Mobile-Responsive Design**  

## 🚀 Features

### 🔐 Authentication & Authorization
- JWT-based authentication with role management
- Admin and Player role separation
- Secure route protection
- Password validation and encoding

### 🎮 Quiz System
- **OpenTDB Integration** - Fresh questions from Open Trivia Database
- **Real-time Timer** - Visual countdown with color coding
- **Multiple Categories** - Science, History, Sports, Entertainment, etc.
- **Difficulty Levels** - Easy, Medium, Hard
- **HTML Entity Support** - Proper rendering of special characters
- **Answer Review** - Detailed feedback on correct/incorrect answers

### 👑 Admin Features
- **Tournament Creation** - Full CRUD operations with validation
- **System Statistics** - Overview of tournaments and participation
- **User Management** - Monitor player performance
- **OpenTDB Health Monitoring** - API connectivity status
- **Cache Management** - Question caching for performance

### 🎯 Player Features
- **Tournament Browser** - View available tournaments
- **Interactive Quiz Interface** - Smooth gameplay experience
- **Performance Tracking** - Personal statistics and history
- **Leaderboards** - Compare with other players
- **Profile Management** - Personal information and preferences

## 🛠 Technology Stack

### Backend
- **Spring Boot 3.1.5** - Main framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **H2/PostgreSQL** - Database options
- **JWT (JJWT 0.11.5)** - Token-based authentication
- **OpenTDB API** - Quiz questions source
- **Maven** - Build tool
- **JaCoCo** - Code coverage
- **Testcontainers** - Integration testing

### Frontend
- **React 18** - UI framework
- **React Router 6** - Navigation and routing
- **Axios** - HTTP client for API communication
- **Context API** - Global state management
- **CSS3** - Modern styling with responsive design
- **HTML5** - Semantic markup

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (tested with OpenJDK 24)
- **Node.js 14+** (tested with Node 18)
- **Maven 3.6+**
- **Git** (for cloning)

### 1. Clone Repository
```bash
git clone https://github.com/bhanuGupta1/quiz-tournament-app.git
cd quiz-tournament-app
```

### 2. Start Backend
```bash
cd backend
mvn spring-boot:run
```
Backend will start on: http://localhost:8080

### 3. Start Frontend
```bash
cd frontend
npm install
npm start
```
Frontend will start on: http://localhost:3000

### 4. Quick Development Start
```bash
# Use the convenience script (Windows)
start-dev.bat
```

## 🔑 Default Credentials

### Admin Access
- **Username**: `admin`
- **Password**: `op@1234`
- **Capabilities**: Full tournament management, system statistics

### Player Registration
- Register new players at: http://localhost:3000/register
- **Required Fields**: Username, First Name, Last Name, Email, Password
- **Optional Fields**: Phone, City, Preferred Category

## 📊 API Documentation & Monitoring

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:quiz_tournament`
  - Username: `sa`
  - Password: (empty)
- **Health Check**: http://localhost:8080/api/health
- **OpenTDB Status**: http://localhost:8080/api/tournaments/questions/health

## 🎮 User Journey

### For Players:
1. **Register** → Create account with personal details
2. **Login** → Access player dashboard
3. **Browse Tournaments** → View available tournaments by category/difficulty
4. **Start Quiz** → Interactive gameplay with OpenTDB questions
5. **View Results** → Detailed score breakdown and answer review
6. **Check Leaderboard** → Compare performance with others
7. **Track Progress** → Personal statistics and history

### For Admins:
1. **Login** → Access admin dashboard
2. **Create Tournaments** → Set up new tournaments with categories/difficulty
3. **Manage Tournaments** → View, edit, delete existing tournaments
4. **Monitor System** → View statistics and API health
5. **View Leaderboards** → Monitor player performance across tournaments

## 🔧 Configuration

### Backend Configuration
Key settings in `backend/src/main/resources/application.properties`:
- Database: H2 in-memory (development) / PostgreSQL (production)
- JWT secret and expiration
- CORS settings for frontend integration
- Email configuration for notifications
- OpenTDB API integration settings

### Frontend Configuration
Environment variables in `frontend/.env`:
- `REACT_APP_API_URL=http://localhost:8080`

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test                    # Unit tests
mvn verify                  # Integration tests
mvn jacoco:report          # Coverage report
```

### Frontend Tests
```bash
cd frontend
npm test                   # Jest tests
npm run build             # Production build test
```

## 🔍 Troubleshooting

### Common Issues:

1. **Backend won't start**
   - Check Java version (17+ required)
   - Verify port 8080 is available
   - Check database configuration

2. **Frontend can't connect to backend**
   - Verify backend is running on port 8080
   - Check CORS configuration
   - Verify API URL in frontend/.env

3. **Quiz questions not loading**
   - Check OpenTDB API connectivity
   - Verify tournament has valid category/difficulty
   - Check backend logs for API errors

4. **Authentication issues**
   - Clear browser localStorage
   - Check JWT token expiration
   - Verify user roles and permissions

## 📈 Performance Features

- **Question Caching** - OpenTDB responses cached per tournament
- **Lazy Loading** - Components loaded on demand
- **Optimized Queries** - Efficient database operations
- **Connection Pooling** - HikariCP for database connections
- **Responsive Design** - Mobile-optimized interface

## 🔒 Security Features

- **JWT Authentication** - Secure token-based auth
- **Role-Based Access Control** - Admin/Player separation
- **Input Validation** - Server-side validation for all inputs
- **CORS Protection** - Configured for frontend domain
- **Password Encryption** - BCrypt hashing
- **SQL Injection Prevention** - JPA/Hibernate protection

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **OpenTDB** - Open Trivia Database for quiz questions
- **Spring Boot** - Excellent framework for rapid development
- **React** - Powerful UI library
- **JWT.io** - JWT implementation and debugging tools

---

**Phase 8 Status**: ✅ **COMPLETE** - Full-stack application with OpenTDB integration, comprehensive UI/UX, and production-ready features.

**Next Steps**: Deploy to production environment, add advanced analytics, implement real-time multiplayer features.
