import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import HealthCheck from '../components/HealthCheck';
import MyAnswersModal from '../components/MyAnswersModal';
import TournamentCard from '../components/TournamentCard';
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

      {/* Quick Links for Players */}
      {user?.role === 'PLAYER' && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>Quick Links</h3>
          <div style={{ display: 'flex', gap: '15px', marginTop: '15px', flexWrap: 'wrap' }}>
            <Link to="/popular-tournaments" className="btn btn-secondary">
              🏆 Popular Tournaments
            </Link>
            <Link to="/my-profile" className="btn btn-secondary">
              👤 My Profile
            </Link>
          </div>
        </div>
      )}

      {/* Tournament Sections for Players */}
      {user?.role === 'PLAYER' ? (
        <div>
          {/* Available Tournaments */}
          {quizzes.filter(tournament => !isCompletedTournament(tournament.id)).length > 0 && (
            <div style={{ marginBottom: '40px' }}>
              <h3 style={{ color: '#007bff', marginBottom: '20px' }}>
                🎯 Available Tournaments ({quizzes.filter(tournament => !isCompletedTournament(tournament.id)).length})
              </h3>
              <div className="quiz-grid">
                {quizzes
                  .filter(tournament => !isCompletedTournament(tournament.id))
                  .map((tournament) => (
                    <TournamentCard
                      key={tournament.id}
                      tournament={tournament}
                      onViewMyAnswers={handleViewMyAnswers}
                      isCompleted={false}
                      showLikeButton={true}
                    />
                  ))}
              </div>
            </div>
          )}

          {/* Completed Tournaments */}
          {quizzes.filter(tournament => isCompletedTournament(tournament.id)).length > 0 && (
            <div style={{ marginBottom: '40px' }}>
              <h3 style={{ color: '#28a745', marginBottom: '20px' }}>
                ✅ Completed Tournaments ({quizzes.filter(tournament => isCompletedTournament(tournament.id)).length})
              </h3>
              <div className="quiz-grid">
                {quizzes
                  .filter(tournament => isCompletedTournament(tournament.id))
                  .map((tournament) => (
                    <TournamentCard
                      key={tournament.id}
                      tournament={tournament}
                      onViewMyAnswers={handleViewMyAnswers}
                      isCompleted={true}
                      showLikeButton={true}
                    />
                  ))}
              </div>
            </div>
          )}

          {/* No tournaments message */}
          {quizzes.length === 0 && (
            <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
              <h3>No tournaments available</h3>
              <p>Check back later for new tournaments!</p>
            </div>
          )}
        </div>
      ) : (
        /* Admin View - All Tournaments */
        <div className="quiz-grid">
          {quizzes.length > 0 ? (
            quizzes.map((tournament) => (
              <TournamentCard
                key={tournament.id}
                tournament={tournament}
                onViewMyAnswers={handleViewMyAnswers}
                isCompleted={isCompletedTournament(tournament.id)}
                showLikeButton={true}
              />
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
      )}

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