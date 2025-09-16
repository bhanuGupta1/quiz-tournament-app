# Backend Deployment Guide (Railway)

## Prerequisites
1. Create a Railway account at https://railway.app
2. Install Railway CLI: `npm install -g @railway/cli`
3. Login to Railway: `railway login`

## Deployment Steps

### 1. Initialize Railway Project
```bash
cd backend
railway login
railway init
```

### 2. Set Environment Variables
```bash
railway variables set SPRING_PROFILES_ACTIVE=prod
railway variables set JWT_SECRET=your-super-secure-jwt-secret-key-here-32-chars-minimum
railway variables set FRONTEND_URL=https://your-frontend-url.netlify.app
```

### 3. Deploy
```bash
railway up
```

### 4. Get Your Backend URL
After deployment, Railway will provide a URL like:
`https://your-app-name.railway.app`

## Alternative: Manual Deployment

### 1. Push to GitHub
```bash
git add .
git commit -m "Prepare backend for Railway deployment"
git push origin main
```

### 2. Connect GitHub to Railway
1. Go to https://railway.app
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Choose your repository
5. Select the `backend` folder as root
6. Set environment variables in Railway dashboard

## Environment Variables to Set in Railway:
- `SPRING_PROFILES_ACTIVE=prod`
- `JWT_SECRET=your-secure-secret-key`
- `FRONTEND_URL=https://your-frontend-domain.netlify.app`

## Health Check
Your backend will be available at: `https://your-app.railway.app/api/tournaments/health`