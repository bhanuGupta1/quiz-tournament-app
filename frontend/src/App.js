import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import ErrorBoundary from './components/ErrorBoundary';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Quiz from './pages/Quiz';
import AdminDashboard from './pages/AdminDashboard';
import CreateTournament from './pages/CreateTournament';
import ManageTournaments from './pages/ManageTournaments';
import TournamentDetails from './pages/TournamentDetails';
import TournamentResults from './pages/TournamentResults';
import Leaderboard from './pages/Leaderboard';
import EnhancedLeaderboard from './pages/EnhancedLeaderboard';
import Profile from './pages/Profile';
import ForgotPassword from './pages/ForgotPassword';
import PopularTournaments from './pages/PopularTournaments';
import { AuthProvider } from './context/AuthContext';
import './App.css';

function App() {
  return (
    <ErrorBoundary>
      <AuthProvider>
        <Router>
          <div className="App">
            <Header />
            <main>
              <ErrorBoundary>
                <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/login" element={<Login />} />
                  <Route path="/register" element={<Register />} />
                  <Route path="/dashboard" element={<Dashboard />} />
                  <Route path="/admin" element={<AdminDashboard />} />
                  <Route path="/admin/create-tournament" element={<CreateTournament />} />
                  <Route path="/admin/tournaments" element={<ManageTournaments />} />
                  <Route path="/admin/tournament/:id/results" element={<TournamentResults />} />
                  <Route path="/tournament/:id" element={<TournamentDetails />} />
                  <Route path="/tournament/:id/quiz" element={<Quiz />} />
                  <Route path="/leaderboard/:id" element={<Leaderboard />} />
                  <Route path="/enhanced-scores/:id" element={<EnhancedLeaderboard />} />
                  <Route path="/profile" element={<Profile />} />
                  <Route path="/my-profile" element={<Profile />} />
                  <Route path="/popular-tournaments" element={<PopularTournaments />} />
                  <Route path="/forgot-password" element={<ForgotPassword />} />
                </Routes>
              </ErrorBoundary>
            </main>
          </div>
        </Router>
      </AuthProvider>
    </ErrorBoundary>
  );
}

export default App;