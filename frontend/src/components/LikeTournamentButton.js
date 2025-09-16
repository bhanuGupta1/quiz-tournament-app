import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const LikeTournamentButton = ({ 
  tournamentId, 
  size = 'normal', // 'small', 'normal', 'large'
  showCount = true,
  className = '',
  style = {},
  isCompleted = false // Only show for completed tournaments
}) => {
  const { user } = useAuth();
  const [liked, setLiked] = useState(false);
  const [likeCount, setLikeCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [eligible, setEligible] = useState(false);

  useEffect(() => {
    const fetchLikeStatus = async () => {
      if (!tournamentId || user?.role !== 'PLAYER') return;

      try {
        // Get current like status
        const statusResponse = await api.get(`/api/tournaments/${tournamentId}/like-status`);
        if (statusResponse.data.success) {
          setLiked(statusResponse.data.userLiked);
          setLikeCount(statusResponse.data.totalLikes);
        }

        // Check if user can like this tournament
        const eligibilityResponse = await api.get(`/api/tournaments/${tournamentId}/like-eligibility`);
        if (eligibilityResponse.data.success) {
          setEligible(eligibilityResponse.data.canLike);
        }
      } catch (error) {
        console.error('Failed to fetch like status:', error);
      }
    };

    fetchLikeStatus();
  }, [tournamentId, user?.role]);

  const handleToggleLike = async () => {
    if (!tournamentId || user?.role !== 'PLAYER' || loading) return;

    setLoading(true);
    try {
      const response = await api.put(`/api/tournaments/${tournamentId}/like`);
      
      if (response.data.success) {
        setLiked(response.data.userLiked);
        setLikeCount(response.data.totalLikes);
      }
    } catch (error) {
      console.error('Failed to toggle like:', error);
      
      // Show appropriate error message
      if (error.response?.status === 403) {
        alert('You need to complete this tournament before you can like it.');
      } else {
        alert('Failed to update like status. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Don't render for non-players or non-completed tournaments
  if (user?.role !== 'PLAYER' || !isCompleted) {
    return null;
  }

  const getSizeStyles = () => {
    switch (size) {
      case 'small':
        return { fontSize: '14px', padding: '4px 8px' };
      case 'large':
        return { fontSize: '20px', padding: '12px 16px' };
      default:
        return { fontSize: '16px', padding: '8px 12px' };
    }
  };

  const buttonStyles = {
    background: 'none',
    border: '1px solid #dee2e6',
    borderRadius: '4px',
    cursor: (loading || !eligible) ? 'not-allowed' : 'pointer',
    display: 'flex',
    alignItems: 'center',
    gap: '5px',
    color: liked ? '#dc3545' : '#6c757d',
    opacity: (loading || !eligible) ? 0.6 : 1,
    transition: 'all 0.2s ease',
    ...getSizeStyles(),
    ...style
  };

  return (
    <button
      onClick={handleToggleLike}
      disabled={loading || !eligible}
      className={className}
      style={buttonStyles}
      title={
        !eligible 
          ? 'Complete this tournament to like it' 
          : liked 
            ? 'Unlike this tournament' 
            : 'Like this tournament'
      }
      onMouseEnter={(e) => {
        if (eligible && !loading) {
          e.target.style.backgroundColor = '#f8f9fa';
          e.target.style.borderColor = '#adb5bd';
        }
      }}
      onMouseLeave={(e) => {
        if (eligible && !loading) {
          e.target.style.backgroundColor = 'transparent';
          e.target.style.borderColor = '#dee2e6';
        }
      }}
    >
      {loading ? (
        <>⏳ {showCount && likeCount}</>
      ) : (
        <>
          {liked ? '❤️' : '🤍'} 
          {showCount && <span>{likeCount}</span>}
        </>
      )}
    </button>
  );
};

export default LikeTournamentButton;