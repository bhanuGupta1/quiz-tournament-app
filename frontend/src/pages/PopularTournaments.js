import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import TournamentCard from '../components/TournamentCard';
import api from '../services/api';

const PopularTournaments = () => {
  const { user } = useAuth();
  const [popularTournaments, setPopularTournaments] = useState([]);
  const [myLikedTournaments, setMyLikedTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('popular');

  useEffect(() => {
    fetchData();
  }, [user]);

  const fetchData = async () => {
    setLoading(true);
    setError('');

    try {
      // Fetch popular tournaments (available to all users)
      const popularResponse = await api.get('/api/tournaments/popular?limit=20');
      if (popularResponse.data.success) {
        setPopularTournaments(popularResponse.data.popularTournaments || []);
      }

      // Fetch user's liked tournaments (only for players)
      if (user?.role === 'PLAYER') {
        try {
          const likedResponse = await api.get('/api/tournaments/my-likes');
          if (likedResponse.data.success) {
            setMyLikedTournaments(likedResponse.data.likedTournaments || []);
          }
        } catch (likedError) {
          console.error('Failed to fetch liked tournaments:', likedError);
          // Don't show error for this, it's optional
        }
      }
    } catch (error) {
      console.error('Failed to fetch tournament data:', error);
      setError('Failed to load tournament data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div>Loading popular tournaments...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      {/* Header */}
      <div style={{ marginBottom: '30px' }}>
        <h1>Tournament Popularity</h1>
        <p>Discover the most loved tournaments by the community</p>
      </div>

      {/* Navigation Tabs */}
      <div style={{ marginBottom: '30px' }}>
        <div style={{ 
          display: 'flex', 
          gap: '10px', 
          borderBottom: '2px solid #dee2e6',
          paddingBottom: '10px'
        }}>
          <button
            onClick={() => setActiveTab('popular')}
            style={{
              padding: '10px 20px',
              border: 'none',
              background: activeTab === 'popular' ? '#007bff' : 'transparent',
              color: activeTab === 'popular' ? 'white' : '#007bff',
              borderRadius: '4px',
              cursor: 'pointer',
              fontWeight: 'bold'
            }}
          >
            🏆 Most Popular ({popularTournaments.length})
          </button>
          
          {user?.role === 'PLAYER' && (
            <button
              onClick={() => setActiveTab('liked')}
              style={{
                padding: '10px 20px',
                border: 'none',
                background: activeTab === 'liked' ? '#007bff' : 'transparent',
                color: activeTab === 'liked' ? 'white' : '#007bff',
                borderRadius: '4px',
                cursor: 'pointer',
                fontWeight: 'bold'
              }}
            >
              ❤️ My Liked ({myLikedTournaments.length})
            </button>
          )}
        </div>
      </div>

      {/* Error Display */}
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
            onClick={fetchData}
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

      {/* Content */}
      {activeTab === 'popular' && (
        <div>
          <div style={{ marginBottom: '20px' }}>
            <h3>🏆 Most Popular Tournaments</h3>
            <p style={{ color: '#6c757d' }}>
              Tournaments ranked by player likes and engagement
            </p>
          </div>

          {popularTournaments.length > 0 ? (
            <div className="quiz-grid">
              {popularTournaments.map((tournamentData) => (
                <div key={tournamentData.tournamentId} className="quiz-card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                    <h3 style={{ margin: 0, flex: 1 }}>{tournamentData.tournamentName}</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                      <span style={{ fontSize: '16px' }}>❤️</span>
                      <span style={{ fontWeight: 'bold', color: '#dc3545' }}>
                        {tournamentData.likeCount}
                      </span>
                    </div>
                  </div>
                  
                  <p>{tournamentData.category} • {tournamentData.difficulty}</p>
                  
                  <div className="quiz-meta">
                    <span>Status: {tournamentData.status}</span>
                    <span>Created by: {tournamentData.createdBy}</span>
                  </div>

                  <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                    <Link 
                      to={`/tournament/${tournamentData.tournamentId}`} 
                      className="btn btn-secondary"
                      style={{ flex: 1 }}
                    >
                      View Details
                    </Link>
                    {user?.role === 'PLAYER' && tournamentData.status === 'ONGOING' && (
                      <Link 
                        to={`/tournament/${tournamentData.tournamentId}/quiz`} 
                        className="btn btn-primary"
                        style={{ flex: 1 }}
                      >
                        Start Quiz
                      </Link>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
              <h4>No popular tournaments yet</h4>
              <p>Be the first to like a tournament and make it popular!</p>
              <Link to="/dashboard" className="btn btn-primary">
                Browse Tournaments
              </Link>
            </div>
          )}
        </div>
      )}

      {activeTab === 'liked' && user?.role === 'PLAYER' && (
        <div>
          <div style={{ marginBottom: '20px' }}>
            <h3>❤️ Your Liked Tournaments</h3>
            <p style={{ color: '#6c757d' }}>
              Tournaments you've liked, sorted by most recent
            </p>
          </div>

          {myLikedTournaments.length > 0 ? (
            <div className="quiz-grid">
              {myLikedTournaments.map((tournamentData) => (
                <div key={tournamentData.tournamentId} className="quiz-card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                    <h3 style={{ margin: 0, flex: 1 }}>{tournamentData.tournamentName}</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                      <span style={{ fontSize: '16px' }}>❤️</span>
                      <span style={{ fontWeight: 'bold', color: '#dc3545' }}>
                        {tournamentData.totalLikes}
                      </span>
                    </div>
                  </div>
                  
                  <p>{tournamentData.category} • {tournamentData.difficulty}</p>
                  
                  <div className="quiz-meta">
                    <span>Status: {tournamentData.status}</span>
                    <span>Liked: {new Date(tournamentData.likedAt).toLocaleDateString()}</span>
                  </div>

                  <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                    <Link 
                      to={`/tournament/${tournamentData.tournamentId}`} 
                      className="btn btn-secondary"
                      style={{ flex: 1 }}
                    >
                      View Details
                    </Link>
                    {tournamentData.status === 'ONGOING' && (
                      <Link 
                        to={`/tournament/${tournamentData.tournamentId}/quiz`} 
                        className="btn btn-primary"
                        style={{ flex: 1 }}
                      >
                        Start Quiz
                      </Link>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
              <h4>No liked tournaments yet</h4>
              <p>Complete some tournaments and like your favorites!</p>
              <Link to="/dashboard" className="btn btn-primary">
                Browse Tournaments
              </Link>
            </div>
          )}
        </div>
      )}

      {/* Back to Dashboard */}
      <div style={{ textAlign: 'center', marginTop: '40px' }}>
        <Link to="/dashboard" className="btn btn-secondary">
          ← Back to Dashboard
        </Link>
      </div>
    </div>
  );
};

export default PopularTournaments;