import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const EnhancedLeaderboard = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [tournamentData, setTournamentData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchEnhancedScores();
  }, [id, user, navigate]);

  const fetchEnhancedScores = async () => {
    try {
      const response = await api.get(`/api/tournaments/${id}/enhanced-scores`);
      
      if (response.data.success) {
        setTournamentData(response.data);
      } else {
        setError(response.data.error || 'Failed to load tournament scores');
      }
    } catch (error) {
      setError('Failed to load tournament scores');
      console.error('Enhanced leaderboard error:', error);
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

  const getScoreColor = (percentage) => {
    if (percentage >= 80) return '#28a745'; // Green
    if (percentage >= 60) return '#ffc107'; // Yellow
    return '#dc3545'; // Red
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatTime = (seconds) => {
    if (!seconds) return 'N/A';
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading tournament scores...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div style={{ 
          background: '#f8d7da', 
          color: '#721c24', 
          padding: '15px', 
          borderRadius: '6px', 
          marginBottom: '20px',
          textAlign: 'center'
        }}>
          {error}
        </div>
        <div style={{ textAlign: 'center' }}>
          <button onClick={() => navigate('/dashboard')} className="btn btn-secondary">
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h1 style={{ margin: 0 }}>🏆 Tournament Scores</h1>
          <Link to="/dashboard" className="btn btn-secondary">
            Back to Dashboard
          </Link>
        </div>
        
        <div style={{ marginBottom: '20px' }}>
          <h2 style={{ color: '#007bff', margin: '0 0 10px 0' }}>
            {tournamentData.tournament.name}
          </h2>
          <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', color: '#666' }}>
            <span><strong>Category:</strong> {tournamentData.tournament.category}</span>
            <span><strong>Difficulty:</strong> {tournamentData.tournament.difficulty}</span>
          </div>
        </div>

        {/* Statistics Summary */}
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
          gap: '20px',
          padding: '20px',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px'
        }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#007bff' }}>
              {tournamentData.statistics.totalPlayers}
            </div>
            <div style={{ fontSize: '14px', color: '#666' }}>Total Players</div>
          </div>
          
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#28a745' }}>
              {tournamentData.statistics.averageScore}%
            </div>
            <div style={{ fontSize: '14px', color: '#666' }}>Average Score</div>
          </div>
          
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#dc3545' }}>
              ❤️ {tournamentData.statistics.likesCount}
            </div>
            <div style={{ fontSize: '14px', color: '#666' }}>Likes</div>
          </div>
          
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ffc107' }}>
              {tournamentData.statistics.passRate}%
            </div>
            <div style={{ fontSize: '14px', color: '#666' }}>Pass Rate</div>
          </div>
        </div>
      </div>

      {/* Scores Table */}
      <div className="card">
        <h3 style={{ marginBottom: '20px' }}>📊 Player Scores (Sorted by Score)</h3>
        
        {tournamentData.scores.length > 0 ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #dee2e6', backgroundColor: '#f8f9fa' }}>
                  <th style={{ padding: '12px', textAlign: 'center' }}>Rank</th>
                  <th style={{ padding: '12px', textAlign: 'left' }}>Player Name</th>
                  <th style={{ padding: '12px', textAlign: 'center' }}>Score</th>
                  <th style={{ padding: '12px', textAlign: 'center' }}>Percentage</th>
                  <th style={{ padding: '12px', textAlign: 'center' }}>Status</th>
                  <th style={{ padding: '12px', textAlign: 'center' }}>Completed Date</th>
                  <th style={{ padding: '12px', textAlign: 'center' }}>Time Taken</th>
                </tr>
              </thead>
              <tbody>
                {tournamentData.scores.map((score, index) => (
                  <tr key={index} style={{ 
                    borderBottom: '1px solid #dee2e6',
                    backgroundColor: score.rank <= 3 ? '#fff3cd' : 'transparent'
                  }}>
                    <td style={{ 
                      padding: '12px', 
                      textAlign: 'center',
                      fontSize: '1.2rem',
                      fontWeight: 'bold'
                    }}>
                      {getRankIcon(score.rank)}
                    </td>
                    
                    <td style={{ padding: '12px' }}>
                      <div style={{ fontWeight: 'bold' }}>{score.playerName}</div>
                      <div style={{ fontSize: '14px', color: '#666' }}>@{score.username}</div>
                    </td>
                    
                    <td style={{ 
                      padding: '12px', 
                      textAlign: 'center',
                      fontWeight: 'bold',
                      fontSize: '1.1rem'
                    }}>
                      {score.score}/{score.totalQuestions}
                    </td>
                    
                    <td style={{ 
                      padding: '12px', 
                      textAlign: 'center',
                      fontWeight: 'bold',
                      color: getScoreColor(score.percentage)
                    }}>
                      {score.percentage}%
                    </td>
                    
                    <td style={{ padding: '12px', textAlign: 'center' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        color: 'white',
                        backgroundColor: score.passed ? '#28a745' : '#dc3545'
                      }}>
                        {score.passed ? 'PASSED' : 'FAILED'}
                      </span>
                    </td>
                    
                    <td style={{ 
                      padding: '12px', 
                      textAlign: 'center',
                      fontSize: '14px',
                      color: '#666'
                    }}>
                      {formatDate(score.completedDate)}
                    </td>
                    
                    <td style={{ 
                      padding: '12px', 
                      textAlign: 'center',
                      fontSize: '14px',
                      color: '#666'
                    }}>
                      {formatTime(score.timeTaken)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            <h4>No scores available</h4>
            <p>No players have completed this tournament yet.</p>
          </div>
        )}
      </div>

      {/* Additional Actions */}
      <div style={{ textAlign: 'center', marginTop: '30px' }}>
        <Link 
          to={`/tournament/${id}`} 
          className="btn btn-primary"
          style={{ marginRight: '15px' }}
        >
          View Tournament Details
        </Link>
        {user?.role === 'PLAYER' && (
          <Link 
            to={`/tournament/${id}/quiz`} 
            className="btn btn-success"
          >
            Take Quiz
          </Link>
        )}
      </div>
    </div>
  );
};

export default EnhancedLeaderboard;