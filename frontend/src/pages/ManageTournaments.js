import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const ManageTournaments = () => {
  const [tournaments, setTournaments] = useState([]);
  const [filteredTournaments, setFilteredTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    status: 'all',
    category: 'all',
    difficulty: 'all'
  });
  
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchTournaments();
  }, [user, navigate]);

  useEffect(() => {
    applyFilters();
  }, [tournaments, filters]);

  const fetchTournaments = async () => {
    try {
      const response = await api.get('/api/tournaments');
      setTournaments(response.data.tournaments || []);
    } catch (error) {
      setError('Failed to load tournaments');
      // Error details available in development mode
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...tournaments];

    if (filters.status !== 'all') {
      filtered = filtered.filter(t => t.status.toLowerCase() === filters.status);
    }

    if (filters.category !== 'all') {
      filtered = filtered.filter(t => t.category === filters.category);
    }

    if (filters.difficulty !== 'all') {
      filtered = filtered.filter(t => t.difficulty === filters.difficulty);
    }

    setFilteredTournaments(filtered);
  };

  const handleFilterChange = (filterType, value) => {
    setFilters(prev => ({
      ...prev,
      [filterType]: value
    }));
  };

  const handleDeleteTournament = async (tournamentId) => {
    if (!window.confirm('Are you sure you want to delete this tournament?')) {
      return;
    }

    try {
      await api.delete(`/api/tournaments/${tournamentId}`);
      setTournaments(prev => prev.filter(t => t.id !== tournamentId));
    } catch (error) {
      alert('Failed to delete tournament');
      // Error details available in development mode
    }
  };

  const getStatusColor = (status) => {
    switch (status.toLowerCase()) {
      case 'upcoming': return '#28a745';
      case 'ongoing': return '#ffc107';
      case 'past': return '#6c757d';
      default: return '#007bff';
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading tournaments...
        </div>
      </div>
    );
  }

  const categories = [...new Set(tournaments.map(t => t.category))];
  const difficulties = [...new Set(tournaments.map(t => t.difficulty))];

  return (
    <div className="container">
      <div style={{ marginBottom: '30px' }}>
        <h1>Manage All Tournaments</h1>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px' }}>
          <p>Total: {filteredTournaments.length} tournaments</p>
          <Link to="/admin/create-tournament" className="btn btn-primary">
            Create New Tournament
          </Link>
        </div>
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

      {/* Filters */}
      <div className="card" style={{ marginBottom: '30px' }}>
        <h3>Filters</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginTop: '20px' }}>
          <div className="form-group">
            <label>Status</label>
            <select 
              value={filters.status} 
              onChange={(e) => handleFilterChange('status', e.target.value)}
            >
              <option value="all">All Statuses</option>
              <option value="upcoming">Upcoming</option>
              <option value="ongoing">Ongoing</option>
              <option value="past">Past</option>
            </select>
          </div>

          <div className="form-group">
            <label>Category</label>
            <select 
              value={filters.category} 
              onChange={(e) => handleFilterChange('category', e.target.value)}
            >
              <option value="all">All Categories</option>
              {categories.map(category => (
                <option key={category} value={category}>{category}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Difficulty</label>
            <select 
              value={filters.difficulty} 
              onChange={(e) => handleFilterChange('difficulty', e.target.value)}
            >
              <option value="all">All Difficulties</option>
              {difficulties.map(difficulty => (
                <option key={difficulty} value={difficulty}>
                  {difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Tournaments Grid */}
      {filteredTournaments.length > 0 ? (
        <div className="quiz-grid">
          {filteredTournaments.map((tournament) => (
            <div key={tournament.id} className="quiz-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                <h4 style={{ margin: 0, flex: 1 }}>{tournament.name}</h4>
                <span 
                  style={{ 
                    padding: '4px 8px', 
                    borderRadius: '4px', 
                    fontSize: '12px', 
                    fontWeight: 'bold',
                    color: 'white',
                    backgroundColor: getStatusColor(tournament.status)
                  }}
                >
                  {tournament.status}
                </span>
              </div>
              
              <p>{tournament.category} • {tournament.difficulty.charAt(0).toUpperCase() + tournament.difficulty.slice(1)}</p>
              
              <div className="quiz-meta">
                <span>Start: {new Date(tournament.startDate).toLocaleDateString()}</span>
                <span>End: {new Date(tournament.endDate).toLocaleDateString()}</span>
              </div>
              
              <div className="quiz-meta">
                <span>Min Score: {tournament.minPassingScore}%</span>
                <span>Created by: {tournament.createdBy?.username || 'Admin'}</span>
              </div>

              <div style={{ display: 'flex', gap: '8px', marginTop: '15px' }}>
                <Link 
                  to={`/tournament/${tournament.id}`} 
                  className="btn btn-secondary"
                  style={{ flex: 1, fontSize: '14px', padding: '8px' }}
                >
                  View
                </Link>
                <Link 
                  to={`/leaderboard/${tournament.id}`} 
                  className="btn btn-primary"
                  style={{ flex: 1, fontSize: '14px', padding: '8px' }}
                >
                  Leaderboard
                </Link>
                <Link 
                  to={`/admin/tournament/${tournament.id}/results`} 
                  className="btn btn-info"
                  style={{ 
                    flex: 1, 
                    fontSize: '14px', 
                    padding: '8px',
                    backgroundColor: '#17a2b8',
                    color: 'white',
                    border: 'none'
                  }}
                >
                  Results
                </Link>
                {process.env.NODE_ENV === 'development' && (
                  <button 
                    onClick={async () => {
                      try {
                        const response = await api.get(`/api/tournaments/${tournament.id}/debug`);
                        console.log('Debug data:', response.data);
                        alert(`Debug Info:\nQuiz Results: ${response.data.quizResultsCount}\nCheck console for details`);
                      } catch (error) {
                        console.error('Debug error:', error);
                        alert('Debug failed: ' + error.message);
                      }
                    }}
                    className="btn"
                    style={{ 
                      flex: 1, 
                      fontSize: '12px', 
                      padding: '8px',
                      backgroundColor: '#6c757d',
                      color: 'white',
                      border: 'none'
                    }}
                  >
                    Debug
                  </button>
                )}
                <button 
                  onClick={() => handleDeleteTournament(tournament.id)}
                  className="btn"
                  style={{ 
                    flex: 1, 
                    fontSize: '14px', 
                    padding: '8px',
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
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <h3>No tournaments found</h3>
          <p>No tournaments match your current filters.</p>
          <button 
            onClick={() => setFilters({ status: 'all', category: 'all', difficulty: 'all' })}
            className="btn btn-secondary"
          >
            Clear Filters
          </button>
        </div>
      )}
    </div>
  );
};

export default ManageTournaments;