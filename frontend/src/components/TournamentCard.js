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
  const [likeEligible, setLikeEligible] = useState(false);

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

        // Check like eligibility
        const eligibilityResponse = await api.get(`/api/tournaments/${tournament.id}/like-eligibility`);
        if (eligibilityResponse.data.success) {
          setLikeEligible(eligibilityResponse.data.canLike);
        }
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

      {/* Like Section - Only for Players */}
      {user?.role === 'PLAYER' && showLikeButton && (
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
            disabled={likeLoading || !likeEligible}
            style={{
              background: 'none',
              border: 'none',
              cursor: (likeLoading || !likeEligible) ? 'not-allowed' : 'pointer',
              fontSize: '18px',
              display: 'flex',
              alignItems: 'center',
              gap: '5px',
              color: liked ? '#dc3545' : '#6c757d',
              opacity: (likeLoading || !likeEligible) ? 0.6 : 1
            }}
            title={
              !likeEligible 
                ? 'Complete this tournament to like it' 
                : liked 
                  ? 'Unlike this tournament' 
                  : 'Like this tournament'
            }
          >
            {likeLoading ? '⏳' : (liked ? '❤️' : '🤍')} {likeCount}
          </button>
          
          {!likeEligible && (
            <span style={{ fontSize: '12px', color: '#6c757d', fontStyle: 'italic' }}>
              Complete to like
            </span>
          )}
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
        
        {user?.role === 'PLAYER' && tournament.status === 'ONGOING' && (
          <Link 
            to={`/tournament/${tournament.id}/quiz`} 
            className="btn btn-primary"
            style={{ flex: 1, minWidth: '100px' }}
          >
            Start Tournament
          </Link>
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