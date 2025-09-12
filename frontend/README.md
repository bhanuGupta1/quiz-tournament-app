# Quiz Tournament Frontend

A React-based frontend for the Quiz Tournament application.

## Features

- User authentication (login/register)
- Quiz browsing and participation
- Real-time quiz gameplay with timer
- Score tracking and results
- Responsive design
- Integration with Spring Boot backend

## Getting Started

### Prerequisites

- Node.js (version 14 or higher)
- npm or yarn

### Installation

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

The application will open in your browser at `http://localhost:3000`.

### Environment Variables

Create a `.env` file in the frontend directory to configure the API URL:

```
REACT_APP_API_URL=http://localhost:8080
```

## Available Scripts

- `npm start` - Runs the app in development mode
- `npm test` - Launches the test runner
- `npm run build` - Builds the app for production
- `npm run eject` - Ejects from Create React App (one-way operation)

## Project Structure

```
src/
├── components/          # Reusable UI components
├── context/            # React context providers
├── pages/              # Page components
├── services/           # API service functions
├── App.js              # Main app component
├── App.css             # Global styles
├── index.js            # Entry point
└── index.css           # Base styles
```

## API Integration

The frontend communicates with the Spring Boot backend through REST APIs. The main endpoints include:

- `/api/auth/login` - User authentication
- `/api/auth/register` - User registration
- `/api/quizzes` - Quiz management
- `/api/quizzes/{id}/submit` - Quiz submission

## Styling

The application uses vanilla CSS with a modern, responsive design. Key features:

- Mobile-first responsive design
- Clean, professional UI
- Consistent color scheme
- Smooth animations and transitions