@echo off
echo ========================================
echo Railway Backend Deployment
echo ========================================

echo Step 1: Installing Railway CLI (if not installed)
npm install -g @railway/cli

echo.
echo Step 2: Login to Railway
railway login

echo.
echo Step 3: Navigate to backend directory
cd backend

echo.
echo Step 4: Initialize Railway project in backend directory
railway init

echo.
echo Step 5: Set environment variables
echo Setting production environment variables...
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set JWT_SECRET=quiz-tournament-super-secure-jwt-secret-key-2024-production-environment
railway variables set FRONTEND_URL=https://majestic-tarsier-882abf.netlify.app
railway variables set PORT=8080

echo.
echo Step 6: Deploy backend
railway up

echo.
echo ========================================
echo Backend Deployment Complete!
echo ========================================
echo.
echo Your backend will be available at:
echo https://your-app-name.railway.app
echo.
echo Next: Deploy frontend to Netlify and update FRONTEND_URL
echo ========================================

pause