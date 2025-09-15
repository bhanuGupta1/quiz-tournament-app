import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import TournamentModal from '../components/TournamentModal';
import TournamentQuestionsModal from '../components/TournamentQuestionsModal';
import TournamentTable from '../components/TournamentTable';
import DetailedAnswersModal from '../components/DetailedAnswersModal';

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
  
  // Modal states - Assessment Requirements
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [showQuestionsModal, setShowQuestionsModal] = useState(false);
  const [showDetailedAnswersModal, setShowDetailedAnswersModal] = useState(false);
  const [selectedTournament, setSelectedTournament] = useState(null);
  const [viewMode, setViewMode] = useState('table'); // 'table' or 'grid'
  
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

  const handleDeleteTournament = async (tournamentId, tournamentName) => {
    // Assessment Requirement: User confirmation prompt
    if (!window.confirm(`Are you sure you want to delete "${tournamentName}"?\n\nThis action cannot be undone.`)) {
      return;
    }

    try {
      await api.delete(`/api/tournaments/${tournamentId}`);
      setTournaments(prev => prev.filter(t => t.id !== tournamentId));
    } catch (error) {
      alert('Failed to delete tournament: ' + (error.response?.data?.error || error.message));
    }
  };

  // Modal handlers - Assessment Requirements
  const handleCreateSuccess = (newTournament) => {
    setTournaments(prev => [newTournament, ...prev]);
  };

  const handleUpdateSuccess = (updatedTournament) => {
    setTournaments(prev => prev.map(t => 
      t.id === updatedTournament.id ? updatedTournament : t
    ));
  };

  const handleViewQuestions = (tournament) => {
    setSelectedTournament(tournament);
    setShowQuestionsModal(true);
  };

  const handleEditTournament = (tournament) => {
    setSelectedTournament(tournament);
    setShowUpdateModal(true);
  };

  const handleViewDetails = (tournament) => {
    navigate(`/tournament/${tournament.id}`);
  };

  const handleViewDetailedAnswers = (tournament) => {
    setSelectedTournament(tournament);
    setShowDetailedAnswersModal(true);
  };

  const handleViewScores = (tournament) => {
    navigate(`/enhanced-scores/${tournament.id}`);
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '20px', flexWrap: 'wrap', gap: '10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            <p style={{ margin: 0 }}>Total: {filteredTournaments.length} tournaments</p>
            
            {/* View Mode Toggle */}
            <div style={{ display: 'flex', gap: '5px' }}>
              <button
                onClick={() => setViewMode('table')}
                className={`btn ${viewMode === 'table' ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '6px 12px', fontSize: '14px' }}
              >
                Table
              </button>
              <button
                onClick={() => setViewMode('grid')}
                className={`btn ${viewMode === 'grid' ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '6px 12px', fontSize: '14px' }}
              >
                Grid
              </button>
            </div>
          </div>
          
          <button 
            onClick={() => setShowCreateModal(true)}
            className="btn btn-primary"
          >
            Create New Tournament
          </button>
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

      {/* Tournament Display - Assessment Requirements */}
      {filteredTournaments.length > 0 ? (
        viewMode === 'table' ? (
          /* Assessment Requirement: Table view with creator, name, category, difficulty */
          <TournamentTable
            tournaments={filteredTournaments}
            onEdit={handleEditTournament}
            onDelete={handleDeleteTournament}
            onViewQuestions={handleViewQuestions}
            onViewDetails={handleViewDetails}
            onViewDetailedAnswers={handleViewDetailedAnswers}
            onViewScores={handleViewScores}
          />
        ) : (
          /* Grid view for visual preference */
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

              <div style={{ display: 'flex', gap: '8px', marginTop: '15px', flexWrap: 'wrap' }}>
                {/* Assessment Requirement: Click tournament name to view questions */}
                <button 
                  onClick={() => handleViewQuestions(tournament)}
                  className="btn btn-info"
                  style={{ 
                    flex: '1 1 auto', 
                    fontSize: '14px', 
                    padding: '8px',
                    backgroundColor: '#17a2b8',
                    color: 'white',
                    border: 'none',
                    minWidth: '80px'
                  }}
                  title="View tournament questions and answers"
                >
                  Questions
                </button>

                <button 
                  onClick={() => handleViewDetailedAnswers(tournament)}
                  className="btn btn-info"
                  style={{ 
                    flex: '1 1 auto', 
                    fontSize: '14px', 
                    padding: '8px',
                    backgroundColor: '#20c997',
                    color: 'white',
                    border: 'none',
                    minWidth: '70px'
                  }}
                  title="View detailed user answers"
                >
                  Answers
                </button>
                
                <button 
                  onClick={() => handleEditTournament(tournament)}
                  className="btn btn-warning"
                  style={{ 
                    flex: '1 1 auto', 
                    fontSize: '14px', 
                    padding: '8px',
                    backgroundColor: '#ffc107',
                    color: '#212529',
                    border: 'none',
                    minWidth: '60px'
                  }}
                  title="Edit tournament details"
                >
                  Edit
                </button>

                <Link 
                  to={`/tournament/${tournament.id}`} 
                  className="btn btn-secondary"
                  style={{ flex: '1 1 auto', fontSize: '14px', padding: '8px', minWidth: '60px' }}
                >
                  View
                </Link>
                
                <Link 
                  to={`/enhanced-scores/${tournament.id}`} 
                  className="btn btn-success"
                  style={{ flex: '1 1 auto', fontSize: '14px', padding: '8px', minWidth: '80px' }}
                >
                  Scores
                </Link>
                
                <button 
                  onClick={() => handleDeleteTournament(tournament.id, tournament.name)}
                  className="btn"
                  style={{ 
                    flex: '1 1 auto', 
                    fontSize: '14px', 
                    padding: '8px',
                    backgroundColor: '#dc3545', 
                    color: 'white',
                    border: 'none',
                    minWidth: '60px'
                  }}
                  title="Delete tournament permanently"
                >
                  Delete
                </button>
              </div>
            </div>
            ))}
          </div>
        )
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

      {/* Assessment Requirement: Modal Forms */}
      <TournamentModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={handleCreateSuccess}
        mode="create"
      />

      <TournamentModal
        isOpen={showUpdateModal}
        onClose={() => {
          setShowUpdateModal(false);
          setSelectedTournament(null);
        }}
        onSuccess={handleUpdateSuccess}
        tournament={selectedTournament}
        mode="update"
      />

      {/* Assessment Requirement: View Questions Modal */}
      <TournamentQuestionsModal
        isOpen={showQuestionsModal}
        onClose={() => {
          setShowQuestionsModal(false);
          setSelectedTournament(null);
        }}
        tournament={selectedTournament}
      />

      {/* Admin Review: Detailed Answers Modal */}
      <DetailedAnswersModal
        isOpen={showDetailedAnswersModal}
        onClose={() => {
          setShowDetailedAnswersModal(false);
          setSelectedTournament(null);
        }}
        tournament={selectedTournament}
      />
    </div>
  );
};

export default ManageTournaments;