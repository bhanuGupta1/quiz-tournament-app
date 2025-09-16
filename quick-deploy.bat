@echo off
echo ========================================
echo Quiz Tournament App - Quick Deploy
echo ========================================

echo.
echo Step 1: Building Backend...
cd backend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo Backend build failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Building Frontend...
cd ..\frontend
call npm run build
if %errorlevel% neq 0 (
    echo Frontend build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build Complete! 
echo ========================================
echo.
echo Next Steps:
echo 1. Backend JAR: backend\target\quiz-backend-0.0.1-SNAPSHOT.jar
echo 2. Frontend Build: frontend\build\
echo.
echo For Railway (Backend):
echo - Upload the JAR file or connect your GitHub repo
echo - Set environment variables (see deploy-backend.md)
echo.
echo For Netlify (Frontend):
echo - Drag the 'frontend\build' folder to netlify.com
echo - Or connect your GitHub repo (see deploy-frontend.md)
echo.
echo See deploy-backend.md and deploy-frontend.md for detailed instructions.
echo ========================================

pause