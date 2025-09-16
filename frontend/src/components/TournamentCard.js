import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const TournamentCard = ({ 
  tournament, 
  onViewMyAnswers,
  isCompleted = false,
  showLikeButton = true
}) => {
  const { user } = useAuth();
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [likeLoading, setLikeLoading] = useState(false);
  // Removed likeEligible since we now only show likes for completed tournaments

  // Fetch like status when component mounts
  useEffect(() => {
    const fetchLikeStatus = async () => {
      if (!tournament?.id || user?.role !== 'PLAYER') return;

      try {
        // Get like status
        const statusResponse = await api.get(`/api/tournaments/${tournament.id}/like-status`);
        if (statusResponse.data.success) {
          setLiked(statusResponse.data.userLiked);
          setLikeCount(statusResponse.data.totalLikes);
        }

        // Like eligibility is now based on completion status only
      } catch (error) {
        console.error('Failed to fetch like status:', error);
        // Don't show error to user, just use default values
      }
    };

    fetchLikeStatus();
  }, [tournament?.id, user?.role]);

  const handleLikeToggle = async () => {
    if (!tournament?.id || user?.role !== 'PLAYER' || likeLoading) return;

    setLikeLoading(true);
    try {
      // Use the toggle endpoint
      const response = await api.put(`/api/tournaments/${tournament.id}/like`);
      
      if (response.data.success) {
        setLiked(response.data.userLiked);
        setLikeCount(response.data.totalLikes);
        
        // Show success message
        const action = response.data.action || (response.data.userLiked ? 'liked' : 'unliked');
        console.log(`Tournament ${action} successfully!`);
      }
    } catch (error) {
      console.error('Failed to toggle like:', error);
      
      // Show user-friendly error message
      if (error.response?.status === 403) {
        alert('You need to complete this tournament before you can like it.');
      } else if (error.response?.status === 401) {
        alert('Please log in to like tournaments.');
      } else {
        alert('Failed to update like status. Please try again.');
      }
    } finally {
      setLikeLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'ongoing': return '#28a745';
      case 'upcoming': return '#ffc107';
      case 'past': return '#6c757d';
      default: return '#007bff';
    }
  };

  const getStatusText = (status) => {
    return status ? status.charAt(0).toUpperCase() + status.slice(1) : 'Unknown';
  };

  return (
    <div className="quiz-card">
      {/* Tournament Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
        <h3 style={{ margin: 0, flex: 1 }}>{tournament.name || tournament.title}</h3>
        <div style={{ display: 'flex', gap: '5px', alignItems: 'center' }}>
          {/* Completion Status Badge */}
          {isCompleted && (
            <span 
              style={{ 
                padding: '4px 8px', 
                borderRadius: '4px', 
                fontSize: '12px', 
                fontWeight: 'bold',
                color: 'white',
                backgroundColor: '#28a745'
              }}
            >
              ✅ COMPLETED
            </span>
          )}
          {/* Tournament Status Badge */}
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
            {getStatusText(tournament.status)}
          </span>
        </div>
      </div>
      
      {/* Tournament Description */}
      <p>{tournament.description || `${tournament.category} tournament`}</p>
      
      {/* Tournament Meta Information */}
      <div className="quiz-meta">
        <span>{tournament.questionCount || 'Multiple'} questions</span>
        <span>{tournament.difficulty ? tournament.difficulty.charAt(0).toUpperCase() + tournament.difficulty.slice(1) : 'Unknown'}</span>
      </div>
      <div className="quiz-meta">
        <span>Category: {tournament.category}</span>
        <span>Min Score: {tournament.minPassingScore || 70}%</span>
      </div>
      <div className="quiz-meta">
        <span>Start: {tournament.startDate ? new Date(tournament.startDate).toLocaleDateString() : 'TBD'}</span>
        <span>End: {tournament.endDate ? new Date(tournament.endDate).toLocaleDateString() : 'TBD'}</span>
      </div>

      {/* Like Section - Only for Players who completed the tournament */}
      {user?.role === 'PLAYER' && showLikeButton && isCompleted && (
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '10px', 
          marginBottom: '15px',
          padding: '8px',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <button
            onClick={handleLikeToggle}
            disabled={likeLoading}
            style={{
              background: 'none',
              border: 'none',
              cursor: likeLoading ? 'not-allowed' : 'pointer',
              fontSize: '18px',
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              color: liked ? '#dc3545' : '#6c757d',
              opacity: likeLoading ? 0.6 : 1
            }}
            title={liked ? 'Unlike this tournament' : 'Like this tournament'}
          >
            {likeLoading ? '⏳' : (liked ? '❤️' : '🤍')} {likeCount}
          </button>
          
          <span style={{ fontSize: '12px', color: '#28a745', fontStyle: 'italic' }}>
            You completed this tournament!
          </span>
        </div>
      )}

      {/* Show like count for non-completed tournaments (read-only) */}
      {user?.role === 'PLAYER' && showLikeButton && !isCompleted && likeCount > 0 && (
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '10px', 
          marginBottom: '15px',
          padding: '8px',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <span style={{ fontSize: '16px', color: '#6c757d' }}>
            🤍 {likeCount} {likeCount === 1 ? 'like' : 'likes'}
          </span>
          <span style={{ fontSize: '12px', color: '#6c757d', fontStyle: 'italic' }}>
            Complete to like this tournament
          </span>
        </div>
      )}

      {/* Admin Like Count Display */}
      {user?.role === 'ADMIN' && (
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '10px', 
          marginBottom: '15px',
          padding: '8px',
          backgroundColor: '#e9ecef',
          borderRadius: '4px'
        }}>
          <span style={{ fontSize: '16px' }}>❤️</span>
          <span style={{ fontWeight: 'bold', color: '#dc3545' }}>
            {likeCount} likes
          </span>
          <span style={{ fontSize: '12px', color: '#6c757d', marginLeft: 'auto' }}>
            from players
          </span>
        </div>
      )}

      {/* Action Buttons */}
      <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        <Link 
          to={`/tournament/${tournament.id}`} 
          className="btn btn-secondary"
          style={{ flex: 1, minWidth: '100px' }}
        >
          View Details
        </Link>
        
        {user?.role === 'PLAYER' && !isCompleted && (
          <Link 
            to={`/tournament/${tournament.id}/quiz`} 
            className="btn btn-primary"
            style={{ flex: 1, minWidth: '100px' }}
          >
            Start Tournament
          </Link>
        )}
        
        {user?.role === 'PLAYER' && isCompleted && (
          <button 
            className="btn"
            style={{ 
              flex: 1, 
              minWidth: '100px',
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              cursor: 'not-allowed'
            }}
            disabled
            title="You have already completed this tournament"
          >
            ✅ Completed
          </button>
        )}
        
        {user?.role === 'PLAYER' && isCompleted && onViewMyAnswers && (
          <button 
            onClick={() => onViewMyAnswers(tournament)}
            className="btn btn-info"
            style={{ 
              flex: 1, 
              minWidth: '100px',
              backgroundColor: '#17a2b8',
              color: 'white',
              border: 'none'
            }}
            title="Review your answers for this tournament"
          >
            My Answers
          </button>
        )}
        
        {user?.role === 'ADMIN' && (
          <Link 
            to={`/leaderboard/${tournament.id}`} 
            className="btn btn-primary"
            style={{ flex: 1, minWidth: '100px' }}
          >
            Leaderboard
          </Link>
        )}
      </div>
    </div>
  );
};

export default TournamentCard;