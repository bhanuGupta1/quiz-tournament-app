import React, { useState, useEffect } from 'react';
import Modal from './Modal';
import api from '../services/api';

const DetailedAnswersModal = ({ 
  isOpen, 
  onClose, 
  tournament 
}) => {
  const [answerData, setAnswerData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedUser, setSelectedUser] = useState('all');

  useEffect(() => {
    if (isOpen && tournament) {
      fetchDetailedAnswers();
    }
  }, [isOpen, tournament]);

  const fetchDetailedAnswers = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await api.get(`/api/tournaments/${tournament.id}/detailed-answers`);
      
      if (response.data.success) {
        setAnswerData(response.data);
      } else {
        setError(response.data.error || 'Failed to load detailed answers');
      }
    } catch (error) {
      if (error.response?.status === 401) {
        setError('You are not authorized to view detailed answers. Please login as admin.');
      } else if (error.response?.status === 403) {
        setError('Access denied. Admin privileges required.');
      } else {
        setError(error.response?.data?.error || 'Failed to load detailed answers');
      }
    } finally {
      setLoading(false);
    }
  };

  const getAnswerStyle = (isCorrect) => {
    return {
      padding: '8px 12px',
      margin: '4px 0',
      borderRadius: '4px',
      backgroundColor: isCorrect ? '#d4edda' : '#f8d7da',
      border: isCorrect ? '1px solid #c3e6cb' : '1px solid #f5c6cb',
      color: isCorrect ? '#155724' : '#721c24'
    };
  };

  const filteredUsers = selectedUser === 'all' 
    ? Object.keys(answerData?.answersByUser || {})
    : [selectedUser];

  return (
    <Modal 
      isOpen={isOpen} 
      onClose={onClose} 
      title={`Detailed Answers: ${tournament?.name || 'Tournament'}`}
      size="large"
    >
      {loading && (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <div>Loading detailed answers...</div>
        </div>
      )}

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

      {!loading && !error && answerData && (
        <div>
          {/* Summary */}
          <div style={{ marginBottom: '20px', padding: '12px', backgroundColor: '#e7f3ff', borderRadius: '4px' }}>
            <strong>Tournament Summary:</strong><br />
            Category: {answerData.tournament.category} | Difficulty: {answerData.tournament.difficulty}<br />
            Total Users: {answerData.totalUsers} | Total Answers: {answerData.totalAnswers}
          </div>

          {/* User Filter */}
          <div style={{ marginBottom: '20px' }}>
            <label htmlFor="userFilter" style={{ marginRight: '10px', fontWeight: 'bold' }}>
              Filter by User:
            </label>
            <select 
              id="userFilter"
              value={selectedUser} 
              onChange={(e) => setSelectedUser(e.target.value)}
              style={{ padding: '5px 10px', borderRadius: '4px', border: '1px solid #ddd' }}
            >
              <option value="all">All Users</option>
              {Object.keys(answerData.answersByUser).map(user => (
                <option key={user} value={user}>{user}</option>
              ))}
            </select>
          </div>

          {/* Detailed Answers */}
          <div style={{ maxHeight: '60vh', overflowY: 'auto' }}>
            {filteredUsers.map((user) => (
              <div key={user} style={{ 
                marginBottom: '30px', 
                padding: '20px', 
                border: '1px solid #e9ecef', 
                borderRadius: '8px',
                backgroundColor: '#fff'
              }}>
                <h4 style={{ 
                  margin: '0 0 15px 0', 
                  color: '#007bff',
                  borderBottom: '2px solid #007bff',
                  paddingBottom: '5px'
                }}>
                  👤 {user}
                </h4>

                {answerData.answersByUser[user].map((answer, index) => (
                  <div key={index} style={{ 
                    marginBottom: '20px', 
                    padding: '15px', 
                    backgroundColor: '#f8f9fa', 
                    borderRadius: '6px',
                    border: '1px solid #e9ecef'
                  }}>
                    <div style={{ marginBottom: '10px' }}>
                      <strong style={{ color: '#495057' }}>
                        Question {answer.questionNumber}:
                      </strong>
                    </div>

                    <div style={{ 
                      fontSize: '15px', 
                      marginBottom: '12px',
                      lineHeight: '1.4',
                      color: '#333'
                    }}>
                      {answer.questionText}
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                      <div>
                        <strong style={{ 
                          color: answer.isCorrect ? '#28a745' : '#dc3545',
                          marginBottom: '5px', 
                          display: 'block' 
                        }}>
                          {answer.isCorrect ? '✓' : '✗'} User's Answer:
                        </strong>
                        <div style={getAnswerStyle(answer.isCorrect)}>
                          {answer.userAnswer}
                        </div>
                      </div>

                      <div>
                        <strong style={{ 
                          color: '#28a745', 
                          marginBottom: '5px', 
                          display: 'block' 
                        }}>
                          ✓ Correct Answer:
                        </strong>
                        <div style={getAnswerStyle(true)}>
                          {answer.correctAnswer}
                        </div>
                      </div>
                    </div>

                    <div style={{ 
                      marginTop: '10px', 
                      fontSize: '12px', 
                      color: '#666',
                      textAlign: 'right'
                    }}>
                      Answered: {new Date(answer.answeredAt).toLocaleString()}
                    </div>
                  </div>
                ))}

                {/* User Summary */}
                <div style={{ 
                  marginTop: '15px', 
                  padding: '10px', 
                  backgroundColor: '#e9ecef', 
                  borderRadius: '4px',
                  textAlign: 'center'
                }}>
                  <strong>
                    User Score: {answerData.answersByUser[user].filter(a => a.isCorrect).length} / {answerData.answersByUser[user].length} correct
                    ({Math.round((answerData.answersByUser[user].filter(a => a.isCorrect).length / answerData.answersByUser[user].length) * 100)}%)
                  </strong>
                </div>
              </div>
            ))}
          </div>

          {Object.keys(answerData.answersByUser).length === 0 && (
            <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
              <p>No detailed answers available for this tournament.</p>
              <p>Detailed answers are only available for quizzes completed after this feature was added.</p>
            </div>
          )}
        </div>
      )}

      <div style={{ marginTop: '20px', textAlign: 'right' }}>
        <button 
          onClick={onClose}
          className="btn btn-secondary"
        >
          Close
        </button>
      </div>
    </Modal>
  );
};

export default DetailedAnswersModal;