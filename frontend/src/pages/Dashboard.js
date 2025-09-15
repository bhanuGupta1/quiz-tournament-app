import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import HealthCheck from '../components/HealthCheck';
import MyAnswersModal from '../components/MyAnswersModal';
import api from '../services/api';

const Dashboard = () => {
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showMyAnswersModal, setShowMyAnswersModal] = useState(false);
  const [selectedTournament, setSelectedTournament] = useState(null);
  const [completedTournaments, setCompletedTournaments] = useState([]);
  
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
      if (user.role === 'PLAYER') {
        fetchCompletedTournaments();
      }
    }, 100);
    
    return () => clearTimeout(timer);
  }, [user, navigate]);

  const fetchQuizzes = async () => {
    try {
      let response;
      
      if (user.role === 'ADMIN') {
        // Admins can see all tournaments
        response = await api.get('/api/tournaments');
        console.log('Admin tournaments response:', response.data);
        setQuizzes(response.data.tournaments || []);
      } else if (user.role === 'PLAYER') {
        // Players see available tournaments
        response = await api.get('/api/participation/available-tournaments');
        console.log('Player available tournaments response:', response.data);
        setQuizzes(response.data.tournaments || []);
      } else {
        // Fallback: try to get tournaments by status
        response = await api.get('/api/tournaments/status/ongoing');
        console.log('Fallback tournaments response:', response.data);
        setQuizzes(response.data.tournaments || []);
      }
      
      // Clear any previous errors on successful load
      setError('');
    } catch (error) {
      console.error('Primary tournament fetch failed:', error);
      
      // If the specific endpoint fails, try a fallback
      try {
        const fallbackResponse = await api.get('/api/tournaments/status/ongoing');
        setQuizzes(fallbackResponse.data.tournaments || []);
        setError(''); // Clear error if fallback works
      } catch (fallbackError) {
        console.error('Fallback tournament fetch failed:', fallbackError);
        
        // Try one more fallback for admins
        if (user.role === 'ADMIN') {
          try {
            const adminFallback = await api.get('/api/tournaments/statistics');
            if (adminFallback.data.success) {
              setError('Tournaments service is running but no tournaments found. Create your first tournament!');
            }
          } catch (adminError) {
            setError('Unable to connect to tournament service. Please check if the backend is running.');
          }
        } else {
          setError('No tournaments available at the moment. Please check back later.');
        }
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchCompletedTournaments = async () => {
    try {
      // Get user's quiz results to see which tournaments they've completed
      const response = await api.get('/api/participation/my-results');
      if (response.data.success) {
        setCompletedTournaments(response.data.results || []);
      }
    } catch (error) {
      console.log('Could not fetch completed tournaments:', error.message);
      // Don't show error for this, it's optional functionality
    }
  };

  const handleViewMyAnswers = (tournament) => {
    setSelectedTournament(tournament);
    setShowMyAnswersModal(true);
  };

  const isCompletedTournament = (tournamentId) => {
    return completedTournaments.some(result => result.tournament?.id === tournamentId);
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
            <button 
              onClick={async () => {
                try {
                  const allTournaments = await api.get('/api/tournaments');
                  console.log('All tournaments:', allTournaments.data);
                  alert(`Found ${allTournaments.data.tournaments?.length || 0} total tournaments`);
                } catch (err) {
                  alert(`Error fetching tournaments: ${err.message}`);
                }
              }}
              className="btn btn-info"
              style={{ backgroundColor: '#17a2b8', color: 'white' }}
            >
              Debug: Check All Tournaments
            </button>
          </div>
        </div>
      )}

      {/* Player Debug Actions */}
      {user?.role === 'PLAYER' && process.env.NODE_ENV === 'development' && (
        <div className="card" style={{ marginBottom: '30px', background: '#f8f9fa' }}>
          <h4>Debug Tools (Development Only)</h4>
          <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
            <button 
              onClick={async () => {
                try {
                  const available = await api.get('/api/participation/available-tournaments');
                  console.log('Available tournaments:', available.data);
                  alert(`Available: ${available.data.tournaments?.length || 0} tournaments`);
                } catch (err) {
                  alert(`Error: ${err.message}`);
                }
              }}
              className="btn btn-info"
              style={{ backgroundColor: '#17a2b8', color: 'white', fontSize: '12px', padding: '6px 12px' }}
            >
              Check Available
            </button>
            <button 
              onClick={async () => {
                try {
                  const ongoing = await api.get('/api/tournaments/status/ongoing');
                  console.log('Ongoing tournaments:', ongoing.data);
                  alert(`Ongoing: ${ongoing.data.tournaments?.length || 0} tournaments`);
                } catch (err) {
                  alert(`Error: ${err.message}`);
                }
              }}
              className="btn btn-info"
              style={{ backgroundColor: '#17a2b8', color: 'white', fontSize: '12px', padding: '6px 12px' }}
            >
              Check Ongoing
            </button>
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
              <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                <Link 
                  to={`/tournament/${tournament.id}`} 
                  className="btn btn-secondary"
                  style={{ flex: 1, minWidth: '100px' }}
                >
                  View Details
                </Link>
                {user?.role === 'PLAYER' && tournament.status === 'ONGOING' && (
                  <Link 
                    to={`/tournament/${tournament.id}/quiz`} 
                    className="btn btn-primary"
                    style={{ flex: 1, minWidth: '100px' }}
                  >
                    Start Tournament
                  </Link>
                )}
                {user?.role === 'PLAYER' && isCompletedTournament(tournament.id) && (
                  <button 
                    onClick={() => handleViewMyAnswers(tournament)}
                    className="btn btn-info"
                    style={{ 
                      flex: 1, 
                      minWidth: '100px',
                      backgroundColor: '#17a2b8',
                      color: 'white',
                      border: 'none'
                    }}
                    title="Review your answers for this tournament"
                  >
                    My Answers
                  </button>
                )}
                {user?.role === 'ADMIN' && (
                  <Link 
                    to={`/leaderboard/${tournament.id}`} 
                    className="btn btn-primary"
                    style={{ flex: 1, minWidth: '100px' }}
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
              <div>
                <Link to="/admin/create-tournament" className="btn btn-primary" style={{ marginTop: '15px' }}>
                  Create First Tournament
                </Link>
                <button 
                  onClick={async () => {
                    try {
                      const health = await api.get('/api/health');
                      alert(`Backend Status: ${health.data.status}\nMessage: ${health.data.message}`);
                    } catch (err) {
                      alert(`Backend Error: ${err.message}\nPlease ensure the backend is running on port 8080`);
                    }
                  }}
                  className="btn btn-secondary" 
                  style={{ marginTop: '15px', marginLeft: '10px' }}
                >
                  Test Backend Connection
                </button>
              </div>
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
      
      {/* Health Check Component for debugging */}
      {process.env.NODE_ENV === 'development' && <HealthCheck />}

      {/* My Answers Modal for Players */}
      <MyAnswersModal
        isOpen={showMyAnswersModal}
        onClose={() => {
          setShowMyAnswersModal(false);
          setSelectedTournament(null);
        }}
        tournament={selectedTournament}
      />
    </div>
  );
};

export default Dashboard;