import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const AdminDashboard = () => {
  const [statistics, setStatistics] = useState(null);
  const [myTournaments, setMyTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchAdminData();
  }, [user, navigate]);

  const fetchAdminData = async () => {
    try {
      const [statsResponse, tournamentsResponse] = await Promise.all([
        api.get('/api/tournaments/statistics'),
        api.get('/api/tournaments/my-tournaments')
      ]);
      
      setStatistics(statsResponse.data);
      setMyTournaments(tournamentsResponse.data.tournaments || []);
    } catch (error) {
      setError('Failed to load admin data');
      // Error logged for debugging
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteTournament = async (tournamentId) => {
    if (!window.confirm('Are you sure you want to delete this tournament?')) {
      return;
    }

    try {
      await api.delete(`/api/tournaments/${tournamentId}`);
      setMyTournaments(prev => prev.filter(t => t.id !== tournamentId));
      // Refresh statistics
      const statsResponse = await api.get('/api/tournaments/statistics');
      setStatistics(statsResponse.data);
    } catch (error) {
      alert('Failed to delete tournament');
      // Error logged for debugging
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading admin dashboard...
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div style={{ marginBottom: '30px' }}>
        <h1>Admin Dashboard</h1>
        <p>Manage tournaments and view system statistics</p>
      </div>

      {error && (
        <div style={{ 
          background: '#f8d7da', 
          color: '#721c24', 
          padding: '12px', 
          borderRadius: '4px', 
          marginBottom: '20px' 
        }}>
          {error}
        </div>
      )}

      {/* Quick Actions */}
      <div className="card" style={{ marginBottom: '30px' }}>
        <h3>Quick Actions</h3>
        <div style={{ display: 'flex', gap: '15px', marginTop: '20px' }}>
          <Link to="/admin/create-tournament" className="btn btn-primary">
            Create New Tournament
          </Link>
          <Link to="/admin/tournaments" className="btn btn-secondary">
            Manage All Tournaments
          </Link>
        </div>
      </div>

      {/* Statistics */}
      {statistics && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>System Statistics</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginTop: '20px' }}>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <h4>Total Tournaments</h4>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#007bff' }}>
                {statistics.totalTournaments}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <h4>Upcoming</h4>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#28a745' }}>
                {statistics.upcomingCount}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <h4>Ongoing</h4>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ffc107' }}>
                {statistics.ongoingCount}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <h4>Completed</h4>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#6c757d' }}>
                {statistics.pastCount}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* My Tournaments */}
      <div className="card">
        <h3>My Tournaments ({myTournaments.length})</h3>
        {myTournaments.length > 0 ? (
          <div className="quiz-grid" style={{ marginTop: '20px' }}>
            {myTournaments.map((tournament) => (
              <div key={tournament.id} className="quiz-card">
                <h4>{tournament.name}</h4>
                <p>{tournament.category} • {tournament.difficulty}</p>
                <div className="quiz-meta">
                  <span>Start: {new Date(tournament.startDate).toLocaleDateString()}</span>
                  <span>End: {new Date(tournament.endDate).toLocaleDateString()}</span>
                </div>
                <div className="quiz-meta">
                  <span>Status: {tournament.status}</span>
                  <span>Min Score: {tournament.minPassingScore}%</span>
                </div>
                <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                  <Link 
                    to={`/tournament/${tournament.id}`} 
                    className="btn btn-secondary"
                    style={{ flex: 1 }}
                  >
                    View
                  </Link>
                  <Link 
                    to={`/leaderboard/${tournament.id}`} 
                    className="btn btn-primary"
                    style={{ flex: 1 }}
                  >
                    Leaderboard
                  </Link>
                  <button 
                    onClick={() => handleDeleteTournament(tournament.id)}
                    className="btn"
                    style={{ 
                      flex: 1, 
                      backgroundColor: '#dc3545', 
                      color: 'white',
                      border: 'none'
                    }}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            <p>You haven't created any tournaments yet.</p>
            <Link to="/admin/create-tournament" className="btn btn-primary">
              Create Your First Tournament
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;