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
      if (user.role === 'ADMIN') {
        // Admins can see all tournaments
        const response = await api.get('/api/tournaments');
        console.log('Admin tournaments response:', response.data);
        setQuizzes(response.data.tournaments || []);
      } else if (user.role === 'PLAYER') {
        // Players see ALL tournaments from all categories
        const [ongoingRes, upcomingRes, pastRes] = await Promise.all([
          api.get('/api/tournaments/status/ongoing'),
          api.get('/api/tournaments/status/upcoming'),
          api.get('/api/tournaments/status/past')
        ]);

        console.log('Ongoing tournaments:', ongoingRes.data.tournaments);
        console.log('Upcoming tournaments:', upcomingRes.data.tournaments);
        console.log('Past tournaments:', pastRes.data.tournaments);

        // Combine all tournaments
        const allTournaments = [
          ...(ongoingRes.data.tournaments || []),
          ...(upcomingRes.data.tournaments || []),
          ...(pastRes.data.tournaments || [])
        ];

        console.log('All tournaments combined:', allTournaments);
        setQuizzes(allTournaments);
      } else {
        // Fallback: try to get tournaments by status
        const response = await api.get('/api/tournaments/status/ongoing');
        console.log('Fallback tournaments response:', response.data);
        setQuizzes(response.data.tournaments || []);
      }
      
      // Clear any previous errors on successful load
      setError('');
    } catch (error) {
      console.error('Primary tournament fetch failed:', error);
      setError('Failed to load tournaments. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const fetchCompletedTournaments = async () => {
    try {
      // Try multiple endpoints to get user's completed tournaments
      let completedResults = [];
      
      // First try the quiz history endpoint
      try {
        const historyResponse = await api.get('/api/participation/my-quiz-history');
        if (historyResponse.data.success) {
          completedResults = historyResponse.data.quizHistory || [];
          console.log('Completed tournaments from history:', completedResults);
        }
      } catch (historyError) {
        console.log('Quiz history endpoint failed:', historyError.message);
      }

      // If that fails, try the my-tournaments endpoint
      if (completedResults.length === 0) {
        try {
          const tournamentsResponse = await api.get('/api/participation/my-tournaments');
          if (tournamentsResponse.data.success) {
            completedResults = tournamentsResponse.data.tournaments || [];
            console.log('Completed tournaments from my-tournaments:', completedResults);
          }
        } catch (tournamentsError) {
          console.log('My-tournaments endpoint failed:', tournamentsError.message);
        }
      }

      setCompletedTournaments(completedResults);
      console.log('Final completed tournaments set:', completedResults);
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
    const isCompleted = completedTournaments.some(result => {
      // Handle different response formats
      const resultTournamentId = result.tournament?.id || result.tournamentId || result.id;
      return resultTournamentId === tournamentId;
    });
    
    console.log(`Tournament ${tournamentId} completion status:`, isCompleted);
    return isCompleted;
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
            <button 
              onClick={async () => {
                try {
                  await fetchCompletedTournaments();
                  alert(`✅ Refreshed! Completed: ${completedTournaments.length} tournaments`);
                } catch (err) {
                  alert(`Error: ${err.message}`);
                }
              }}
              className="btn btn-success"
              style={{ backgroundColor: '#28a745', color: 'white', fontSize: '12px', padding: '6px 12px' }}
            >
              Refresh Completed
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
          {/* Debug Info */}
          {process.env.NODE_ENV === 'development' && (
            <div style={{ 
              background: '#f8f9fa', 
              padding: '10px', 
              borderRadius: '4px', 
              marginBottom: '20px',
              fontSize: '12px'
            }}>
              <strong>Debug Info:</strong><br/>
              Total tournaments: {quizzes.length}<br/>
              Completed tournaments: {completedTournaments.length}<br/>
              Completed IDs: {completedTournaments.map(t => t.tournament?.id || t.tournamentId || t.id).join(', ')}
            </div>
          )}

          {/* Ongoing Tournaments */}
          {quizzes.filter(tournament => tournament.status === 'ONGOING').length > 0 && (
            <div style={{ marginBottom: '40px' }}>
              <h3 style={{ color: '#28a745', marginBottom: '20px' }}>
                🟢 Ongoing Tournaments ({quizzes.filter(tournament => tournament.status === 'ONGOING').length})
              </h3>
              <div className="quiz-grid">
                {quizzes
                  .filter(tournament => tournament.status === 'ONGOING')
                  .map((tournament) => (
                    <TournamentCard
                      key={tournament.id}
                      tournament={tournament}
                      onViewMyAnswers={handleViewMyAnswers}
                      isCompleted={isCompletedTournament(tournament.id)}
                      showLikeButton={true}
                    />
                  ))}
              </div>
            </div>
          )}

          {/* Upcoming Tournaments */}
          {quizzes.filter(tournament => tournament.status === 'UPCOMING').length > 0 && (
            <div style={{ marginBottom: '40px' }}>
              <h3 style={{ color: '#ffc107', marginBottom: '20px' }}>
                🟡 Upcoming Tournaments ({quizzes.filter(tournament => tournament.status === 'UPCOMING').length})
              </h3>
              <div className="quiz-grid">
                {quizzes
                  .filter(tournament => tournament.status === 'UPCOMING')
                  .map((tournament) => (
                    <TournamentCard
                      key={tournament.id}
                      tournament={tournament}
                      onViewMyAnswers={handleViewMyAnswers}
                      isCompleted={isCompletedTournament(tournament.id)}
                      showLikeButton={true}
                    />
                  ))}
              </div>
            </div>
          )}

          {/* Past Tournaments */}
          {quizzes.filter(tournament => tournament.status === 'PAST').length > 0 && (
            <div style={{ marginBottom: '40px' }}>
              <h3 style={{ color: '#6c757d', marginBottom: '20px' }}>
                🔴 Past Tournaments ({quizzes.filter(tournament => tournament.status === 'PAST').length})
              </h3>
              <div className="quiz-grid">
                {quizzes
                  .filter(tournament => tournament.status === 'PAST')
                  .map((tournament) => (
                    <TournamentCard
                      key={tournament.id}
                      tournament={tournament}
                      onViewMyAnswers={handleViewMyAnswers}
                      isCompleted={isCompletedTournament(tournament.id)}
                      showLikeButton={true}
                    />
                  ))}
              </div>
            </div>
          )}

          {/* Completed Tournaments Section */}
          {completedTournaments.length > 0 && (
            <div style={{ marginBottom: '40px' }}>
              <h3 style={{ color: '#17a2b8', marginBottom: '20px' }}>
                ✅ My Completed Tournaments ({completedTournaments.length})
              </h3>
              <div className="quiz-grid">
                {completedTournaments.map((result) => {
                  const tournamentId = result.tournament?.id || result.tournamentId || result.id;
                  const tournament = quizzes.find(t => t.id === tournamentId) || {
                    id: tournamentId,
                    name: result.tournament?.name || result.tournamentName || 'Unknown Tournament',
                    category: result.tournament?.category || 'Unknown',
                    difficulty: result.tournament?.difficulty || 'Unknown',
                    status: 'COMPLETED'
                  };
                  
                  return (
                    <TournamentCard
                      key={tournamentId}
                      tournament={tournament}
                      onViewMyAnswers={handleViewMyAnswers}
                      isCompleted={true}
                      showLikeButton={true}
                    />
                  );
                })}
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
        /* Admin View - Redirect to Professional Dashboard */
        <div className="card" style={{ textAlign: 'center', padding: '60px' }}>
          <div style={{ fontSize: '4rem', marginBottom: '20px' }}>🎯</div>
          <h3>Admin Control Center Available</h3>
          <p style={{ color: '#6c757d', marginBottom: '30px' }}>
            Access the professional admin dashboard for comprehensive tournament management, analytics, and system oversight.
          </p>
          <Link 
            to="/admin" 
            className="btn btn-primary"
            style={{ 
              padding: '15px 30px',
              fontSize: '18px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              border: 'none',
              textDecoration: 'none',
              color: 'white'
            }}
          >
            🚀 Go to Admin Control Center
          </Link>
          
          <div style={{ marginTop: '20px', fontSize: '14px', color: '#6c757d' }}>
            <p>Features available in Admin Control Center:</p>
            <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', marginTop: '10px' }}>
              <span>📊 Analytics</span>
              <span>🎯 Tournament Management</span>
              <span>❤️ Like Statistics</span>
              <span>🏆 Leaderboards</span>
              <span>📝 Question Management</span>
            </div>
          </div>
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