import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const Dashboard = () => {
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    
    // Add a small delay to ensure user data is fully loaded
    const timer = setTimeout(() => {
      fetchQuizzes();
    }, 100);
    
    return () => clearTimeout(timer);
  }, [user, navigate]);

  const fetchQuizzes = async () => {
    try {
      let response;
      
      if (user.role === 'ADMIN') {
        // Admins can see all tournaments
        response = await api.get('/api/tournaments');
        setQuizzes(response.data.tournaments || []);
      } else if (user.role === 'PLAYER') {
        // Players see available tournaments
        response = await api.get('/api/participation/available-tournaments');
        setQuizzes(response.data.tournaments || []);
      } else {
        // Fallback: try to get tournaments by status
        response = await api.get('/api/tournaments/status/ongoing');
        setQuizzes(response.data.tournaments || []);
      }
    } catch (error) {
      console.error('Error fetching tournaments:', error);
      
      // If the specific endpoint fails, try a fallback
      try {
        const fallbackResponse = await api.get('/api/tournaments/status/ongoing');
        setQuizzes(fallbackResponse.data.tournaments || []);
        setError(''); // Clear error if fallback works
      } catch (fallbackError) {
        setError('Failed to load tournaments. Please try again later.');
        console.error('Fallback also failed:', fallbackError);
      }
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading...
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div style={{ marginBottom: '30px' }}>
        <h1>Welcome back, {user?.username}!</h1>
        {user?.role === 'ADMIN' ? (
          <p>Manage tournaments and view system overview</p>
        ) : (
          <p>Choose a tournament to participate in</p>
        )}
      </div>

      {/* Admin Quick Actions */}
      {user?.role === 'ADMIN' && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>Quick Actions</h3>
          <div style={{ display: 'flex', gap: '15px', marginTop: '15px', flexWrap: 'wrap' }}>
            <Link to="/admin" className="btn btn-primary">
              Admin Dashboard
            </Link>
            <Link to="/admin/create-tournament" className="btn btn-secondary">
              Create Tournament
            </Link>
            <Link to="/admin/tournaments" className="btn btn-secondary">
              Manage All Tournaments
            </Link>
          </div>
        </div>
      )}

      {error && (
        <div style={{ 
          background: '#f8d7da', 
          color: '#721c24', 
          padding: '12px', 
          borderRadius: '4px', 
          marginBottom: '20px' 
        }}>
          {error}
          <button 
            onClick={() => {
              setError('');
              setLoading(true);
              fetchQuizzes();
            }}
            style={{ 
              marginLeft: '10px', 
              padding: '4px 8px', 
              background: '#721c24', 
              color: 'white', 
              border: 'none', 
              borderRadius: '3px',
              cursor: 'pointer'
            }}
          >
            Retry
          </button>
        </div>
      )}

      <div className="quiz-grid">
        {quizzes.length > 0 ? (
          quizzes.map((tournament) => (
            <div key={tournament.id} className="quiz-card">
              <h3>{tournament.name || tournament.title}</h3>
              <p>{tournament.description || `${tournament.category} tournament`}</p>
              <div className="quiz-meta">
                <span>{tournament.questionCount || 'Multiple'} questions</span>
                <span>{tournament.difficulty ? tournament.difficulty.charAt(0).toUpperCase() + tournament.difficulty.slice(1) : 'Unknown'}</span>
              </div>
              <div className="quiz-meta">
                <span>Category: {tournament.category}</span>
                <span>Status: {tournament.status ? tournament.status.charAt(0).toUpperCase() + tournament.status.slice(1) : 'Unknown'}</span>
              </div>
              <div className="quiz-meta">
                <span>Start: {tournament.startDate ? new Date(tournament.startDate).toLocaleDateString() : 'TBD'}</span>
                <span>End: {tournament.endDate ? new Date(tournament.endDate).toLocaleDateString() : 'TBD'}</span>
              </div>
              <div style={{ display: 'flex', gap: '10px' }}>
                <Link 
                  to={`/tournament/${tournament.id}`} 
                  className="btn btn-secondary"
                  style={{ flex: 1 }}
                >
                  View Details
                </Link>
                {user?.role === 'PLAYER' && tournament.status === 'ONGOING' && (
                  <Link 
                    to={`/tournament/${tournament.id}/quiz`} 
                    className="btn btn-primary"
                    style={{ flex: 1 }}
                  >
                    Start Tournament
                  </Link>
                )}
                {user?.role === 'ADMIN' && (
                  <Link 
                    to={`/leaderboard/${tournament.id}`} 
                    className="btn btn-primary"
                    style={{ flex: 1 }}
                  >
                    Leaderboard
                  </Link>
                )}
              </div>
            </div>
          ))
        ) : (
          <div className="card" style={{ gridColumn: '1 / -1', textAlign: 'center' }}>
            <h3>No tournaments available</h3>
            <p>
              {user?.role === 'ADMIN' 
                ? 'Create your first tournament to get started!' 
                : 'Check back later for new tournaments!'
              }
            </p>
            {user?.role === 'ADMIN' && (
              <Link to="/admin/create-tournament" className="btn btn-primary" style={{ marginTop: '15px' }}>
                Create First Tournament
              </Link>
            )}
          </div>
        )}
      </div>

      <div style={{ marginTop: '40px', textAlign: 'center' }}>
        <h3>Your Stats</h3>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '30px', marginTop: '20px' }}>
          <div className="card" style={{ minWidth: '150px' }}>
            <h4>Tournaments Taken</h4>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#007bff' }}>
              {user?.tournamentsParticipated || 0}
            </div>
          </div>
          <div className="card" style={{ minWidth: '150px' }}>
            <h4>Average Score</h4>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#28a745' }}>
              {user?.averageScore || 0}%
            </div>
          </div>
          <div className="card" style={{ minWidth: '150px' }}>
            <h4>City</h4>
            <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: '#ffc107' }}>
              {user?.city || 'Not set'}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;