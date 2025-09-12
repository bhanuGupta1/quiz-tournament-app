import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const TournamentDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [tournament, setTournament] = useState(null);
  const [eligibility, setEligibility] = useState(null);
  const [statistics, setStatistics] = useState(null);
  const [userResult, setUserResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchTournamentData();
  }, [id, user, navigate]);

  const fetchTournamentData = async () => {
    try {
      const tournamentResponse = await api.get(`/api/tournaments/${id}`);
      setTournament(tournamentResponse.data.tournament);

      // Fetch additional data for players
      if (user.role === 'PLAYER') {
        try {
          const eligibilityResponse = await api.get(`/api/participation/tournaments/${id}/eligibility`);
          setEligibility(eligibilityResponse.data);
        } catch (error) {
          // Eligibility check failed - handled gracefully
        }

        try {
          const resultResponse = await api.get(`/api/participation/tournaments/${id}/my-result`);
          setUserResult(resultResponse.data.result);
        } catch (error) {
          // No previous result - expected for new participants
        }
      }

      // Fetch statistics for everyone
      try {
        const statsResponse = await api.get(`/api/participation/tournaments/${id}/statistics`);
        setStatistics(statsResponse.data.statistics);
      } catch (error) {
        // Statistics not available - handled gracefully
      }

    } catch (error) {
      setError('Failed to load tournament details');
      // Error details available in development mode
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'upcoming': return '#28a745';
      case 'ongoing': return '#ffc107';
      case 'past': return '#6c757d';
      default: return '#007bff';
    }
  };

  const canParticipate = () => {
    return user.role === 'PLAYER' && 
           eligibility?.eligible && 
           tournament?.status === 'ONGOING' &&
           !userResult;
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading tournament details...
        </div>
      </div>
    );
  }

  if (error || !tournament) {
    return (
      <div className="container">
        <div className="card">
          <h2>Tournament Not Found</h2>
          <p>{error || 'The tournament you are looking for does not exist.'}</p>
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
          <div>
            <h1 style={{ margin: 0, marginBottom: '10px' }}>{tournament.name}</h1>
            <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
              <span style={{ 
                padding: '6px 12px', 
                borderRadius: '6px', 
                fontSize: '14px', 
                fontWeight: 'bold',
                color: 'white',
                backgroundColor: getStatusColor(tournament.status)
              }}>
                {tournament.status}
              </span>
              <span>{tournament.category}</span>
              <span>{tournament.difficulty}</span>
            </div>
          </div>
          
          {canParticipate() && (
            <Link 
              to={`/tournament/${id}/quiz`} 
              className="btn btn-primary"
              style={{ fontSize: '18px', padding: '12px 24px' }}
            >
              Start Tournament
            </Link>
          )}
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
          <div>
            <strong>Start Date:</strong>
            <div>{new Date(tournament.startDate).toLocaleDateString()}</div>
          </div>
          <div>
            <strong>End Date:</strong>
            <div>{new Date(tournament.endDate).toLocaleDateString()}</div>
          </div>
          <div>
            <strong>Minimum Passing Score:</strong>
            <div>{tournament.minPassingScore}%</div>
          </div>
          <div>
            <strong>Created By:</strong>
            <div>{tournament.createdBy?.username || 'Admin'}</div>
          </div>
        </div>
      </div>

      {/* Player-specific sections */}
      {user.role === 'PLAYER' && (
        <>
          {/* Eligibility Status */}
          {eligibility && (
            <div className="card" style={{ marginBottom: '30px' }}>
              <h3>Participation Status</h3>
              {eligibility.eligible ? (
                <div style={{ color: '#28a745' }}>
                  ✅ You are eligible to participate in this tournament
                </div>
              ) : (
                <div style={{ color: '#dc3545' }}>
                  ❌ {eligibility.reason || 'You are not eligible to participate'}
                </div>
              )}
            </div>
          )}

          {/* User Result */}
          {userResult && (
            <div className="card" style={{ marginBottom: '30px' }}>
              <h3>Your Result</h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '20px' }}>
                <div style={{ textAlign: 'center' }}>
                  <strong>Score</strong>
                  <div style={{ fontSize: '2rem', fontWeight: 'bold', color: userResult.passed ? '#28a745' : '#dc3545' }}>
                    {userResult.percentage}%
                  </div>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <strong>Status</strong>
                  <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: userResult.passed ? '#28a745' : '#dc3545' }}>
                    {userResult.passed ? 'PASSED' : 'FAILED'}
                  </div>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <strong>Grade</strong>
                  <div style={{ fontSize: '1.2rem', fontWeight: 'bold' }}>
                    {userResult.grade}
                  </div>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <strong>Completed</strong>
                  <div>{new Date(userResult.completedAt).toLocaleDateString()}</div>
                </div>
              </div>
              {userResult.performanceMessage && (
                <div style={{ marginTop: '15px', padding: '10px', background: '#f8f9fa', borderRadius: '4px' }}>
                  {userResult.performanceMessage}
                </div>
              )}
            </div>
          )}
        </>
      )}

      {/* Statistics */}
      {statistics && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>Tournament Statistics</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '20px' }}>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Total Participants</strong>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#007bff' }}>
                {statistics.totalParticipants || 0}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Average Score</strong>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#28a745' }}>
                {statistics.averageScore ? `${statistics.averageScore.toFixed(1)}%` : 'N/A'}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Pass Rate</strong>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ffc107' }}>
                {statistics.passRate ? `${statistics.passRate.toFixed(1)}%` : 'N/A'}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Highest Score</strong>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#dc3545' }}>
                {statistics.highestScore ? `${statistics.highestScore}%` : 'N/A'}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Action Buttons */}
      <div className="card">
        <h3>Actions</h3>
        <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap' }}>
          <Link to={`/leaderboard/${id}`} className="btn btn-primary">
            View Leaderboard
          </Link>
          
          {user.role === 'ADMIN' && (
            <Link to="/admin/tournaments" className="btn btn-secondary">
              Manage Tournaments
            </Link>
          )}
          
          <button onClick={() => navigate('/dashboard')} className="btn btn-secondary">
            Back to Dashboard
          </button>
        </div>
      </div>
    </div>
  );
};

export default TournamentDetails;