import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const Profile = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [quizHistory, setQuizHistory] = useState([]);
  const [participatedTournaments, setParticipatedTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    fetchProfileData();
  }, [user, navigate]);

  const fetchProfileData = async () => {
    try {
      if (user.role === 'PLAYER') {
        const [historyResponse, tournamentsResponse] = await Promise.all([
          api.get('/api/participation/my-quiz-history'),
          api.get('/api/participation/my-tournaments')
        ]);
        
        setQuizHistory(historyResponse.data.quizHistory || []);
        setParticipatedTournaments(tournamentsResponse.data.tournaments || []);
      }
    } catch (error) {
      setError('Failed to load profile data');
      console.error('Error fetching profile data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getGradeColor = (grade) => {
    switch (grade) {
      case 'A': return '#28a745';
      case 'B': return '#17a2b8';
      case 'C': return '#ffc107';
      case 'D': return '#fd7e14';
      case 'F': return '#dc3545';
      default: return '#6c757d';
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading profile...
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      {/* Profile Header */}
      <div className="card" style={{ marginBottom: '30px' }}>
        <h1>👤 My Profile</h1>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginTop: '20px' }}>
          <div>
            <strong>Username:</strong>
            <div>{user.username}</div>
          </div>
          <div>
            <strong>Name:</strong>
            <div>{user.firstName} {user.lastName}</div>
          </div>
          <div>
            <strong>Email:</strong>
            <div>{user.email}</div>
          </div>
          <div>
            <strong>Role:</strong>
            <div style={{ 
              padding: '4px 8px', 
              borderRadius: '4px', 
              backgroundColor: user.role === 'ADMIN' ? '#dc3545' : '#007bff',
              color: 'white',
              display: 'inline-block',
              fontSize: '14px',
              fontWeight: 'bold'
            }}>
              {user.role}
            </div>
          </div>
          {user.city && (
            <div>
              <strong>City:</strong>
              <div>{user.city}</div>
            </div>
          )}
          {user.phoneNumber && (
            <div>
              <strong>Phone:</strong>
              <div>{user.phoneNumber}</div>
            </div>
          )}
          {user.preferredCategory && (
            <div>
              <strong>Preferred Category:</strong>
              <div>{user.preferredCategory}</div>
            </div>
          )}
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

      {/* Player Statistics */}
      {user.role === 'PLAYER' && quizHistory.length > 0 && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>📊 My Statistics</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '20px', marginTop: '20px' }}>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Tournaments Completed</strong>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#007bff' }}>
                {quizHistory.length}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Average Score</strong>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#28a745' }}>
                {quizHistory.length > 0 
                  ? `${(quizHistory.reduce((sum, quiz) => sum + quiz.percentage, 0) / quizHistory.length).toFixed(1)}%`
                  : '0%'
                }
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Tournaments Passed</strong>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#28a745' }}>
                {quizHistory.filter(quiz => quiz.passed).length}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Pass Rate</strong>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ffc107' }}>
                {quizHistory.length > 0 
                  ? `${((quizHistory.filter(quiz => quiz.passed).length / quizHistory.length) * 100).toFixed(1)}%`
                  : '0%'
                }
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Quiz History for Players */}
      {user.role === 'PLAYER' && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>🏆 Tournament History</h3>
          {quizHistory.length > 0 ? (
            <div style={{ overflowX: 'auto', marginTop: '20px' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid #dee2e6' }}>
                    <th style={{ padding: '12px', textAlign: 'left' }}>Tournament</th>
                    <th style={{ padding: '12px', textAlign: 'center' }}>Score</th>
                    <th style={{ padding: '12px', textAlign: 'center' }}>Grade</th>
                    <th style={{ padding: '12px', textAlign: 'center' }}>Status</th>
                    <th style={{ padding: '12px', textAlign: 'center' }}>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {quizHistory.map((quiz, index) => (
                    <tr key={index} style={{ borderBottom: '1px solid #dee2e6' }}>
                      <td style={{ padding: '12px' }}>
                        <div style={{ fontWeight: 'bold' }}>
                          {quiz.tournament?.name || `Tournament ${quiz.tournamentId}`}
                        </div>
                        <div style={{ fontSize: '14px', color: '#666' }}>
                          {quiz.tournament?.category} • {quiz.tournament?.difficulty}
                        </div>
                      </td>
                      <td style={{ padding: '12px', textAlign: 'center' }}>
                        <span style={{ 
                          fontSize: '1.1rem', 
                          fontWeight: 'bold',
                          color: quiz.passed ? '#28a745' : '#dc3545'
                        }}>
                          {quiz.percentage}%
                        </span>
                      </td>
                      <td style={{ padding: '12px', textAlign: 'center' }}>
                        <span style={{
                          padding: '4px 8px',
                          borderRadius: '4px',
                          fontWeight: 'bold',
                          color: 'white',
                          backgroundColor: getGradeColor(quiz.grade)
                        }}>
                          {quiz.grade}
                        </span>
                      </td>
                      <td style={{ padding: '12px', textAlign: 'center' }}>
                        <span style={{
                          padding: '4px 8px',
                          borderRadius: '4px',
                          fontSize: '12px',
                          fontWeight: 'bold',
                          color: 'white',
                          backgroundColor: quiz.passed ? '#28a745' : '#dc3545'
                        }}>
                          {quiz.passed ? 'PASSED' : 'FAILED'}
                        </span>
                      </td>
                      <td style={{ padding: '12px', textAlign: 'center', fontSize: '14px', color: '#666' }}>
                        {new Date(quiz.completedAt).toLocaleDateString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
              <h4>No tournament history yet</h4>
              <p>Start participating in tournaments to see your history here!</p>
              <button onClick={() => navigate('/dashboard')} className="btn btn-primary">
                Browse Tournaments
              </button>
            </div>
          )}
        </div>
      )}

      {/* Admin Info */}
      {user.role === 'ADMIN' && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>🛠️ Admin Tools</h3>
          <p>As an administrator, you have access to:</p>
          <ul style={{ marginLeft: '20px', lineHeight: '1.8' }}>
            <li>Create and manage tournaments</li>
            <li>View all tournament statistics</li>
            <li>Monitor participant performance</li>
            <li>Access system analytics</li>
          </ul>
          <div style={{ marginTop: '20px' }}>
            <button onClick={() => navigate('/admin')} className="btn btn-primary">
              Go to Admin Dashboard
            </button>
          </div>
        </div>
      )}

      {/* Back Button */}
      <div style={{ textAlign: 'center' }}>
        <button onClick={() => navigate('/dashboard')} className="btn btn-secondary">
          Back to Dashboard
        </button>
      </div>
    </div>
  );
};

export default Profile;