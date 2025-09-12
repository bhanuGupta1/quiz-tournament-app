import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Quiz from './pages/Quiz';
import AdminDashboard from './pages/AdminDashboard';
import CreateTournament from './pages/CreateTournament';
import ManageTournaments from './pages/ManageTournaments';
import TournamentDetails from './pages/TournamentDetails';
import Leaderboard from './pages/Leaderboard';
import Profile from './pages/Profile';
import { AuthProvider } from './context/AuthContext';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Header />
          <main>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/admin" element={<AdminDashboard />} />
              <Route path="/admin/create-tournament" element={<CreateTournament />} />
              <Route path="/admin/tournaments" element={<ManageTournaments />} />
              <Route path="/tournament/:id" element={<TournamentDetails />} />
              <Route path="/tournament/:id/quiz" element={<Quiz />} />
              <Route path="/leaderboard/:id" element={<Leaderboard />} />
              <Route path="/profile" element={<Profile />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;