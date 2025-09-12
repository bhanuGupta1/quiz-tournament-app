import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const Leaderboard = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [tournament, setTournament] = useState(null);
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [limit, setLimit] = useState(10);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchLeaderboardData();
  }, [id, user, navigate, limit]);

  const fetchLeaderboardData = async () => {
    try {
      const [tournamentResponse, leaderboardResponse] = await Promise.all([
        api.get(`/api/tournaments/${id}`),
        api.get(`/api/participation/tournaments/${id}/leaderboard?limit=${limit}`)
      ]);
      
      console.log('Leaderboard API response:', leaderboardResponse.data);
      console.log('Leaderboard entries:', leaderboardResponse.data.leaderboard);
      
      setTournament(tournamentResponse.data.tournament);
      setLeaderboard(leaderboardResponse.data.leaderboard || []);
    } catch (error) {
      setError('Failed to load leaderboard data');
      // Error details available in development mode
    } finally {
      setLoading(false);
    }
  };

  const getRankIcon = (rank) => {
    switch (rank) {
      case 1: return '🥇';
      case 2: return '🥈';
      case 3: return '🥉';
      default: return `#${rank}`;
    }
  };

  const getScoreColor = (percentage, passed) => {
    if (passed) return '#28a745';
    return '#dc3545';
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading leaderboard...
        </div>
      </div>
    );
  }

  if (error || !tournament) {
    return (
      <div className="container">
        <div className="card">
          <h2>Leaderboard Not Available</h2>
          <p>{error || 'Unable to load leaderboard for this tournament.'}</p>
          <button onClick={() => navigate('/dashboard')} className="btn btn-primary">
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      {/* Tournament Header */}
      <div className="card" style={{ marginBottom: '30px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ margin: 0, marginBottom: '10px' }}>🏆 {tournament.name} Leaderboard</h1>
            <div style={{ display: 'flex', gap: '15px', alignItems: 'center', color: '#666' }}>
              <span>{tournament.category}</span>
              <span>•</span>
              <span>{tournament.difficulty}</span>
              <span>•</span>
              <span>Min Score: {tournament.minPassingScore}%</span>
            </div>
          </div>
          <Link to={`/tournament/${id}`} className="btn btn-secondary">
            Tournament Details
          </Link>
        </div>
      </div>

      {/* Leaderboard Controls */}
      <div className="card" style={{ marginBottom: '30px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3>Rankings ({leaderboard.length} participants)</h3>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <label htmlFor="limit">Show top:</label>
            <select 
              id="limit"
              value={limit} 
              onChange={(e) => setLimit(parseInt(e.target.value))}
              style={{ padding: '5px' }}
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
              <option value={0}>All</option>
            </select>
          </div>
        </div>
      </div>

      {/* Leaderboard */}
      {leaderboard.length > 0 ? (
        <div className="card">
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #dee2e6' }}>
                  <th style={{ padding: '15px', textAlign: 'left' }}>Rank</th>
                  <th style={{ padding: '15px', textAlign: 'left' }}>Player</th>
                  <th style={{ padding: '15px', textAlign: 'center' }}>Score</th>
                  <th style={{ padding: '15px', textAlign: 'center' }}>Status</th>
                  <th style={{ padding: '15px', textAlign: 'center' }}>Grade</th>
                  <th style={{ padding: '15px', textAlign: 'center' }}>Completed</th>
                </tr>
              </thead>
              <tbody>
                {leaderboard.map((result, index) => (
                  <tr 
                    key={result.id || index}
                    style={{ 
                      borderBottom: '1px solid #dee2e6',
                      backgroundColor: result.userId === user.id ? '#f8f9ff' : 'transparent'
                    }}
                  >
                    <td style={{ padding: '15px' }}>
                      <div style={{ 
                        fontSize: index < 3 ? '1.5rem' : '1rem',
                        fontWeight: 'bold'
                      }}>
                        {getRankIcon(index + 1)}
                      </div>
                    </td>
                    <td style={{ padding: '15px' }}>
                      <div>
                        <div style={{ fontWeight: 'bold' }}>
                          {result.playerName || 'Anonymous'}
                          {result.userId === user.id && (
                            <span style={{ 
                              marginLeft: '8px', 
                              padding: '2px 6px', 
                              background: '#007bff', 
                              color: 'white', 
                              borderRadius: '3px', 
                              fontSize: '12px' 
                            }}>
                              You
                            </span>
                          )}
                        </div>
                        <div style={{ fontSize: '14px', color: '#666' }}>
                          User ID: {result.userId}
                        </div>
                      </div>
                    </td>
                    <td style={{ padding: '15px', textAlign: 'center' }}>
                      <div style={{ 
                        fontSize: '1.2rem', 
                        fontWeight: 'bold',
                        color: getScoreColor(result.percentage, result.passed)
                      }}>
                        {result.percentage}%
                      </div>
                    </td>
                    <td style={{ padding: '15px', textAlign: 'center' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        color: 'white',
                        backgroundColor: result.passed ? '#28a745' : '#dc3545'
                      }}>
                        {result.passed ? 'PASSED' : 'FAILED'}
                      </span>
                    </td>
                    <td style={{ padding: '15px', textAlign: 'center', fontWeight: 'bold' }}>
                      {result.grade}
                    </td>
                    <td style={{ padding: '15px', textAlign: 'center', fontSize: '14px', color: '#666' }}>
                      {new Date(result.completedAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {leaderboard.length === 0 && (
            <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
              <h3>No participants yet</h3>
              <p>Be the first to participate in this tournament!</p>
            </div>
          )}
        </div>
      ) : (
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <h3>No Results Available</h3>
          <p>No one has completed this tournament yet.</p>
          {user.role === 'PLAYER' && tournament.status === 'ONGOING' && (
            <Link to={`/tournament/${id}/quiz`} className="btn btn-primary">
              Be the First to Participate!
            </Link>
          )}
        </div>
      )}

      {/* Back Button */}
      <div style={{ textAlign: 'center', marginTop: '30px' }}>
        <button onClick={() => navigate('/dashboard')} className="btn btn-secondary">
          Back to Dashboard
        </button>
      </div>
    </div>
  );
};

export default Leaderboard;