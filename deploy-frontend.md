# Frontend Deployment Guide (Netlify)

## Prerequisites
1. Create a Netlify account at https://netlify.com
2. Install Netlify CLI: `npm install -g netlify-cli`

## Deployment Steps

### Option 1: Drag & Drop Deployment (Easiest)

1. **Build the frontend:**
```bash
cd frontend
npm run build
```

2. **Deploy to Netlify:**
- Go to https://netlify.com
- Drag the `frontend/build` folder to the deploy area
- Your site will be deployed instantly!

### Option 2: Git-based Deployment (Recommended)

1. **Push to GitHub:**
```bash
git add .
git commit -m "Prepare frontend for Netlify deployment"
git push origin main
```

2. **Connect GitHub to Netlify:**
- Go to https://netlify.com
- Click "New site from Git"
- Choose GitHub and select your repository
- Set build settings:
  - **Base directory:** `frontend`
  - **Build command:** `npm run build`
  - **Publish directory:** `frontend/build`

3. **Set Environment Variables in Netlify:**
- Go to Site settings > Environment variables
- Add: `REACT_APP_API_URL` = `https://your-backend-url.railway.app`

### Option 3: CLI Deployment

1. **Build and deploy:**
```bash
cd frontend
npm run build
netlify deploy --prod --dir=build
```

## After Deployment

1. **Update Backend CORS:**
   - Update your backend's `FRONTEND_URL` environment variable in Railway
   - Set it to your Netlify URL (e.g., `https://your-app.netlify.app`)

2. **Update Frontend API URL:**
   - Update `REACT_APP_API_URL` in Netlify environment variables
   - Set it to your Railway backend URL

## Your URLs will be:
- **Frontend:** `https://your-app-name.netlify.app`
- **Backend:** `https://your-app-name.railway.app`

## Test Your Deployment
Visit your frontend URL and try:
1. User registration/login
2. Creating tournaments (admin)
3. Taking quizzes (player)
4. Viewing statistics