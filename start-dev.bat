@echo off
echo Starting Quiz Tournament Development Environment...

echo.
echo Starting Backend (Spring Boot)...
start "Backend" cmd /k "cd backend && mvn spring-boot:run"

echo.
echo Waiting for backend to start...
timeout /t 10 /nobreak > nul

echo.
echo Starting Frontend (React)...
start "Frontend" cmd /k "cd frontend && npm start"

echo.
echo Development environment started!
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
echo.
pause