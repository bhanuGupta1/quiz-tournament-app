import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import TournamentModal from '../components/TournamentModal';
import TournamentQuestionsModal from '../components/TournamentQuestionsModal';
import DetailedAnswersModal from '../components/DetailedAnswersModal';
import api from '../services/api';

const ProfessionalAdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('overview');
  const [statistics, setStatistics] = useState(null);
  const [tournaments, setTournaments] = useState([]);
  const [popularTournaments, setPopularTournaments] = useState([]);
  const [recentResults, setRecentResults] = useState([]);
  const [systemHealth, setSystemHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showQuestionsModal, setShowQuestionsModal] = useState(false);
  const [showAnswersModal, setShowAnswersModal] = useState(false);
  const [selectedTournament, setSelectedTournament] = useState(null);
  
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchAllData();
  }, [user, navigate]);

  const fetchAllData = async () => {
    setLoading(true);
    try {
      const [
        statsResponse,
        tournamentsResponse,
        popularResponse,
        healthResponse
      ] = await Promise.all([
        api.get('/api/tournaments/statistics'),
        api.get('/api/tournaments'),
        api.get('/api/tournaments/popular?limit=5'),
        api.get('/api/tournaments/health')
      ]);
      
      setStatistics(statsResponse.data);
      setTournaments(tournamentsResponse.data.tournaments || []);
      setPopularTournaments(popularResponse.data.popularTournaments || []);
      setSystemHealth(healthResponse.data);

      // Fetch recent results for active tournaments
      if (tournamentsResponse.data.tournaments?.length > 0) {
        const resultsPromises = tournamentsResponse.data.tournaments
          .slice(0, 3)
          .map(t => api.get(`/api/tournaments/${t.id}/enhanced-scores`).catch(() => null));
        
        const resultsResponses = await Promise.all(resultsPromises);
        const validResults = resultsResponses
          .filter(r => r && r.data.success)
          .flatMap(r => r.data.scores.slice(0, 5));
        
        setRecentResults(validResults);
      }
    } catch (error) {
      console.error('Failed to load admin data:', error);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteTournament = async (tournamentId) => {
    if (!window.confirm('Are you sure you want to delete this tournament? This action cannot be undone.')) {
      return;
    }

    try {
      await api.delete(`/api/tournaments/${tournamentId}`);
      setTournaments(prev => prev.filter(t => t.id !== tournamentId));
      await fetchAllData(); // Refresh all data
      alert('Tournament deleted successfully');
    } catch (error) {
      console.error('Failed to delete tournament:', error);
      alert('Failed to delete tournament. Please try again.');
    }
  };

  const handleEditTournament = (tournament) => {
    setSelectedTournament(tournament);
    setShowEditModal(true);
  };

  const handleViewQuestions = (tournament) => {
    setSelectedTournament(tournament);
    setShowQuestionsModal(true);
  };

  const handleViewAnswers = (tournament) => {
    setSelectedTournament(tournament);
    setShowAnswersModal(true);
  };

  const handleModalSuccess = () => {
    fetchAllData(); // Refresh all data after any changes
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '60vh',
          flexDirection: 'column'
        }}>
          <div style={{ 
            width: '50px', 
            height: '50px', 
            border: '4px solid #f3f3f3',
            borderTop: '4px solid #007bff',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
            marginBottom: '20px'
          }}></div>
          <h3>Loading Admin Dashboard...</h3>
        </div>
      </div>
    );
  }

  const renderOverviewTab = () => (
    <div>
      {/* System Health Status */}
      {systemHealth && (
        <div className="card" style={{ marginBottom: '30px', border: '2px solid #28a745' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            <div style={{ fontSize: '2rem' }}>🟢</div>
            <div>
              <h3 style={{ margin: 0, color: '#28a745' }}>System Status: Operational</h3>
              <p style={{ margin: 0, color: '#6c757d' }}>
                All services running normally • Last updated: {new Date().toLocaleTimeString()}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Key Metrics */}
      {statistics && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3 style={{ marginBottom: '20px' }}>📊 System Overview</h3>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
            gap: '20px' 
          }}>
            <div style={{ 
              textAlign: 'center', 
              padding: '25px', 
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              borderRadius: '12px',
              boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
            }}>
              <div style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>
                {statistics.totalTournaments || 0}
              </div>
              <div style={{ fontSize: '14px', opacity: 0.9 }}>Total Tournaments</div>
            </div>
            
            <div style={{ 
              textAlign: 'center', 
              padding: '25px', 
              background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
              color: 'white',
              borderRadius: '12px',
              boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
            }}>
              <div style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>
                {statistics.ongoingCount || 0}
              </div>
              <div style={{ fontSize: '14px', opacity: 0.9 }}>Active Now</div>
            </div>
            
            <div style={{ 
              textAlign: 'center', 
              padding: '25px', 
              background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
              color: 'white',
              borderRadius: '12px',
              boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
            }}>
              <div style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>
                {statistics.upcomingCount || 0}
              </div>
              <div style={{ fontSize: '14px', opacity: 0.9 }}>Upcoming</div>
            </div>
            
            <div style={{ 
              textAlign: 'center', 
              padding: '25px', 
              background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
              color: 'white',
              borderRadius: '12px',
              boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
            }}>
              <div style={{ fontSize: '2.5rem', fontWeight: 'bold' }}>
                {statistics.pastCount || 0}
              </div>
              <div style={{ fontSize: '14px', opacity: 0.9 }}>Completed</div>
            </div>
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="card" style={{ marginBottom: '30px' }}>
        <h3 style={{ marginBottom: '20px' }}>⚡ Quick Actions</h3>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', 
          gap: '15px' 
        }}>
          <button 
            onClick={() => setShowCreateModal(true)}
            className="btn btn-primary"
            style={{ 
              padding: '15px 20px',
              fontSize: '16px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              border: 'none',
              borderRadius: '8px'
            }}
          >
            🎯 Create New Tournament
          </button>
          
          <button 
            onClick={() => setActiveTab('tournaments')}
            className="btn btn-secondary"
            style={{ 
              padding: '15px 20px',
              fontSize: '16px',
              borderRadius: '8px'
            }}
          >
            📋 Manage All Tournaments
          </button>
          
          <button 
            onClick={() => setActiveTab('analytics')}
            className="btn btn-info"
            style={{ 
              padding: '15px 20px',
              fontSize: '16px',
              backgroundColor: '#17a2b8',
              borderRadius: '8px'
            }}
          >
            📈 View Analytics
          </button>
          
          <button 
            onClick={() => window.open('/api/tournaments/statistics', '_blank')}
            className="btn btn-success"
            style={{ 
              padding: '15px 20px',
              fontSize: '16px',
              borderRadius: '8px'
            }}
          >
            🔍 System Reports
          </button>
        </div>
      </div>

      {/* Recent Activity */}
      {recentResults.length > 0 && (
        <div className="card">
          <h3 style={{ marginBottom: '20px' }}>🕒 Recent Activity</h3>
          <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
            {recentResults.map((result, index) => (
              <div key={index} style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                padding: '12px 0',
                borderBottom: index < recentResults.length - 1 ? '1px solid #eee' : 'none'
              }}>
                <div>
                  <strong>{result.username}</strong> completed <em>{result.tournamentName}</em>
                </div>
                <div style={{ 
                  padding: '4px 12px',
                  borderRadius: '20px',
                  backgroundColor: result.percentage >= 70 ? '#d4edda' : '#f8d7da',
                  color: result.percentage >= 70 ? '#155724' : '#721c24',
                  fontSize: '14px',
                  fontWeight: 'bold'
                }}>
                  {result.percentage}%
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );

  const renderTournamentsTab = () => (
    <div>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '20px' 
      }}>
        <h3>🎯 Tournament Management ({tournaments.length})</h3>
        <button 
          onClick={() => setShowCreateModal(true)}
          className="btn btn-primary"
          style={{ borderRadius: '8px' }}
        >
          ➕ Create New Tournament
        </button>
      </div>

      {tournaments.length > 0 ? (
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', 
          gap: '20px' 
        }}>
          {tournaments.map((tournament) => (
            <div key={tournament.id} className="card" style={{ 
              border: '1px solid #dee2e6',
              borderRadius: '12px',
              overflow: 'hidden',
              boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
              {/* Tournament Header */}
              <div style={{ 
                background: tournament.status === 'ONGOING' ? '#28a745' : 
                           tournament.status === 'UPCOMING' ? '#ffc107' : '#6c757d',
                color: 'white',
                padding: '15px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <h4 style={{ margin: 0 }}>{tournament.name}</h4>
                <span style={{ 
                  padding: '4px 8px',
                  backgroundColor: 'rgba(255,255,255,0.2)',
                  borderRadius: '4px',
                  fontSize: '12px'
                }}>
                  {tournament.status}
                </span>
              </div>

              {/* Tournament Details */}
              <div style={{ padding: '15px' }}>
                <div style={{ marginBottom: '10px' }}>
                  <strong>Category:</strong> {tournament.category}<br/>
                  <strong>Difficulty:</strong> {tournament.difficulty}<br/>
                  <strong>Min Score:</strong> {tournament.minPassingScore}%
                </div>
                
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between',
                  fontSize: '14px',
                  color: '#6c757d',
                  marginBottom: '15px'
                }}>
                  <span>Start: {new Date(tournament.startDate).toLocaleDateString()}</span>
                  <span>End: {new Date(tournament.endDate).toLocaleDateString()}</span>
                </div>

                {/* Like Count */}
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '10px',
                  marginBottom: '15px',
                  padding: '8px',
                  backgroundColor: '#f8f9fa',
                  borderRadius: '6px'
                }}>
                  <span style={{ fontSize: '16px' }}>❤️</span>
                  <span style={{ fontWeight: 'bold', color: '#dc3545' }}>
                    {tournament.likeCount || 0} likes
                  </span>
                  <span style={{ fontSize: '12px', color: '#6c757d', marginLeft: 'auto' }}>
                    from players
                  </span>
                </div>

                {/* Action Buttons */}
                <div style={{ 
                  display: 'grid', 
                  gridTemplateColumns: 'repeat(2, 1fr)', 
                  gap: '8px' 
                }}>
                  <button 
                    onClick={() => handleViewQuestions(tournament)}
                    className="btn btn-info"
                    style={{ 
                      fontSize: '12px',
                      padding: '8px',
                      backgroundColor: '#17a2b8'
                    }}
                  >
                    📝 Questions
                  </button>
                  
                  <button 
                    onClick={() => handleViewAnswers(tournament)}
                    className="btn btn-success"
                    style={{ 
                      fontSize: '12px',
                      padding: '8px'
                    }}
                  >
                    📊 Answers
                  </button>
                  
                  <Link 
                    to={`/enhanced-scores/${tournament.id}`}
                    className="btn btn-primary"
                    style={{ 
                      fontSize: '12px',
                      padding: '8px',
                      textAlign: 'center',
                      textDecoration: 'none'
                    }}
                  >
                    🏆 Leaderboard
                  </Link>
                  
                  <button 
                    onClick={() => handleEditTournament(tournament)}
                    className="btn btn-warning"
                    style={{ 
                      fontSize: '12px',
                      padding: '8px'
                    }}
                  >
                    ✏️ Edit
                  </button>
                </div>

                {/* Danger Zone */}
                <div style={{ 
                  marginTop: '15px',
                  paddingTop: '15px',
                  borderTop: '1px solid #dee2e6'
                }}>
                  <button 
                    onClick={() => handleDeleteTournament(tournament.id)}
                    className="btn"
                    style={{ 
                      width: '100%',
                      fontSize: '12px',
                      padding: '8px',
                      backgroundColor: '#dc3545',
                      color: 'white',
                      border: 'none'
                    }}
                  >
                    🗑️ Delete Tournament
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="card" style={{ textAlign: 'center', padding: '60px' }}>
          <div style={{ fontSize: '4rem', marginBottom: '20px' }}>🎯</div>
          <h3>No Tournaments Created Yet</h3>
          <p style={{ color: '#6c757d', marginBottom: '30px' }}>
            Create your first tournament to get started with the quiz platform.
          </p>
          <button 
            onClick={() => setShowCreateModal(true)}
            className="btn btn-primary"
            style={{ 
              padding: '15px 30px',
              fontSize: '16px',
              borderRadius: '8px'
            }}
          >
            🎯 Create First Tournament
          </button>
        </div>
      )}
    </div>
  );

  const renderAnalyticsTab = () => (
    <div>
      <h3 style={{ marginBottom: '20px' }}>📈 Analytics & Insights</h3>
      
      {/* Popular Tournaments */}
      {popularTournaments.length > 0 && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h4>🏆 Most Popular Tournaments</h4>
          <div style={{ marginTop: '15px' }}>
            {popularTournaments.map((tournament, index) => (
              <div key={tournament.tournamentId} style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center',
                padding: '12px 0',
                borderBottom: index < popularTournaments.length - 1 ? '1px solid #eee' : 'none'
              }}>
                <div>
                  <strong>#{index + 1} {tournament.tournamentName}</strong>
                  <div style={{ fontSize: '14px', color: '#6c757d' }}>
                    {tournament.category} • {tournament.difficulty}
                  </div>
                </div>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '5px',
                  color: '#dc3545',
                  fontWeight: 'bold'
                }}>
                  ❤️ {tournament.likeCount}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* System Statistics */}
      {statistics && (
        <div className="card">
          <h4>📊 Detailed Statistics</h4>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', 
            gap: '15px',
            marginTop: '15px'
          }}>
            <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#007bff' }}>
                {((statistics.ongoingCount / (statistics.totalTournaments || 1)) * 100).toFixed(1)}%
              </div>
              <div style={{ fontSize: '14px', color: '#6c757d' }}>Active Rate</div>
            </div>
            
            <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#28a745' }}>
                {statistics.totalParticipants || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#6c757d' }}>Total Players</div>
            </div>
            
            <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ffc107' }}>
                {statistics.averageScore ? `${statistics.averageScore.toFixed(1)}%` : 'N/A'}
              </div>
              <div style={{ fontSize: '14px', color: '#6c757d' }}>Avg Score</div>
            </div>
            
            <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#dc3545' }}>
                {statistics.totalLikes || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#6c757d' }}>Total Likes</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  return (
    <div className="container" style={{ maxWidth: '1400px' }}>
      {/* Header */}
      <div style={{ 
        marginBottom: '30px',
        padding: '30px',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        borderRadius: '15px',
        color: 'white',
        textAlign: 'center'
      }}>
        <h1 style={{ margin: 0, fontSize: '2.5rem', fontWeight: 'bold' }}>
          🎯 Admin Control Center
        </h1>
        <p style={{ margin: '10px 0 0 0', fontSize: '1.2rem', opacity: 0.9 }}>
          Professional Tournament Management Dashboard
        </p>
      </div>

      {/* Error Display */}
      {error && (
        <div style={{ 
          background: '#f8d7da', 
          color: '#721c24', 
          padding: '15px', 
          borderRadius: '8px', 
          marginBottom: '20px',
          border: '1px solid #f5c6cb'
        }}>
          <strong>⚠️ Error:</strong> {error}
          <button 
            onClick={fetchAllData}
            style={{ 
              marginLeft: '15px', 
              padding: '5px 10px', 
              background: '#721c24', 
              color: 'white', 
              border: 'none', 
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Retry
          </button>
        </div>
      )}

      {/* Navigation Tabs */}
      <div style={{ 
        display: 'flex', 
        gap: '5px', 
        marginBottom: '30px',
        borderBottom: '2px solid #dee2e6',
        paddingBottom: '0'
      }}>
        {[
          { id: 'overview', label: '📊 Overview', icon: '📊' },
          { id: 'tournaments', label: '🎯 Tournaments', icon: '🎯' },
          { id: 'analytics', label: '📈 Analytics', icon: '📈' }
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            style={{
              padding: '15px 25px',
              border: 'none',
              background: activeTab === tab.id ? '#007bff' : 'transparent',
              color: activeTab === tab.id ? 'white' : '#007bff',
              borderRadius: '8px 8px 0 0',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold',
              transition: 'all 0.3s ease'
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && renderOverviewTab()}
      {activeTab === 'tournaments' && renderTournamentsTab()}
      {activeTab === 'analytics' && renderAnalyticsTab()}

      {/* Modals */}
      <TournamentModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={handleModalSuccess}
        mode="create"
      />

      <TournamentModal
        isOpen={showEditModal}
        onClose={() => {
          setShowEditModal(false);
          setSelectedTournament(null);
        }}
        onSuccess={handleModalSuccess}
        mode="edit"
        tournament={selectedTournament}
      />

      <TournamentQuestionsModal
        isOpen={showQuestionsModal}
        onClose={() => {
          setShowQuestionsModal(false);
          setSelectedTournament(null);
        }}
        tournament={selectedTournament}
      />

      <DetailedAnswersModal
        isOpen={showAnswersModal}
        onClose={() => {
          setShowAnswersModal(false);
          setSelectedTournament(null);
        }}
        tournament={selectedTournament}
      />

      {/* CSS for animations */}
      <style jsx>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default ProfessionalAdminDashboard;