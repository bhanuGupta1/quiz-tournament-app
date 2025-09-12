@echo off
echo Starting Quiz Tournament Development Environment...

echo.
echo Checking if ports are available...
netstat -an | find "8080" > nul
if %errorlevel% == 0 (
    echo WARNING: Port 8080 is already in use!
    echo Please stop any existing backend services.
    pause
    exit /b 1
)

netstat -an | find "3000" > nul
if %errorlevel% == 0 (
    echo WARNING: Port 3000 is already in use!
    echo Please stop any existing frontend services.
    pause
    exit /b 1
)

echo.
echo Starting Backend (Spring Boot)...
start "Quiz Backend" cmd /k "cd backend && echo Starting Spring Boot backend... && mvn spring-boot:run"

echo.
echo Waiting for backend to start...
timeout /t 15 /nobreak > nul

echo.
echo Testing backend connection...
curl -s http://localhost:8080/api/health > nul
if %errorlevel% == 0 (
    echo Backend is running successfully!
) else (
    echo WARNING: Backend may not be ready yet. Check the backend window.
)

echo.
echo Starting Frontend (React)...
start "Quiz Frontend" cmd /k "cd frontend && echo Starting React frontend... && npm start"

echo.
echo Development environment started!
echo.
echo Backend API: http://localhost:8080
echo Frontend App: http://localhost:3000
echo API Health: http://localhost:8080/api/health
echo H2 Console: http://localhost:8080/h2-console
echo.
echo Default Admin Login:
echo Username: admin
echo Password: op@1234
echo.
echo Press any key to exit...
pause > nul