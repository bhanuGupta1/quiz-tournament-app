import React, { useState, useEffect } from 'react';
import Modal from './Modal';
import api from '../services/api';

const MyAnswersModal = ({ 
  isOpen, 
  onClose, 
  tournament 
}) => {
  const [answerData, setAnswerData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen && tournament) {
      fetchMyAnswers();
    }
  }, [isOpen, tournament]);

  const fetchMyAnswers = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await api.get(`/api/tournaments/${tournament.id}/my-answers`);
      
      if (response.data.success) {
        setAnswerData(response.data);
      } else {
        setError(response.data.error || 'Failed to load your answers');
      }
    } catch (error) {
      if (error.response?.status === 401) {
        setError('You are not authorized to view these answers. Please login.');
      } else if (error.response?.status === 400) {
        setError(error.response?.data?.error || 'You have not completed this tournament yet.');
      } else {
        setError(error.response?.data?.error || 'Failed to load your answers');
      }
    } finally {
      setLoading(false);
    }
  };

  const getAnswerStyle = (isCorrect) => {
    return {
      padding: '10px 15px',
      margin: '8px 0',
      borderRadius: '6px',
      backgroundColor: isCorrect ? '#d4edda' : '#f8d7da',
      border: isCorrect ? '2px solid #c3e6cb' : '2px solid #f5c6cb',
      color: isCorrect ? '#155724' : '#721c24',
      fontWeight: '500'
    };
  };

  const getScoreColor = (percentage) => {
    if (percentage >= 80) return '#28a745'; // Green
    if (percentage >= 60) return '#ffc107'; // Yellow
    return '#dc3545'; // Red
  };

  return (
    <Modal 
      isOpen={isOpen} 
      onClose={onClose} 
      title={`My Quiz Review: ${tournament?.name || 'Tournament'}`}
      size="large"
    >
      {loading && (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <div>Loading your answers...</div>
        </div>
      )}

      {error && (
        <div style={{ 
          background: '#f8d7da', 
          color: '#721c24', 
          padding: '15px', 
          borderRadius: '6px', 
          marginBottom: '20px',
          textAlign: 'center'
        }}>
          {error}
        </div>
      )}

      {!loading && !error && answerData && (
        <div>
          {/* Quiz Summary */}
          <div style={{ 
            marginBottom: '25px', 
            padding: '20px', 
            backgroundColor: '#e7f3ff', 
            borderRadius: '8px',
            border: '1px solid #b3d9ff'
          }}>
            <h4 style={{ margin: '0 0 15px 0', color: '#0066cc' }}>
              📊 Your Performance Summary
            </h4>
            
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
              <div>
                <strong>Tournament:</strong> {answerData.tournament.name}<br />
                <strong>Category:</strong> {answerData.tournament.category}<br />
                <strong>Difficulty:</strong> {answerData.tournament.difficulty}
              </div>
              
              {answerData.quizResult && (
                <div>
                  <strong>Your Score:</strong> {answerData.correctAnswers}/{answerData.totalQuestions}<br />
                  <strong>Percentage:</strong> 
                  <span style={{ 
                    color: getScoreColor(answerData.quizResult.percentage),
                    fontWeight: 'bold',
                    marginLeft: '5px'
                  }}>
                    {answerData.quizResult.percentage.toFixed(1)}%
                  </span><br />
                  <strong>Result:</strong> 
                  <span style={{ 
                    color: answerData.quizResult.passed ? '#28a745' : '#dc3545',
                    fontWeight: 'bold',
                    marginLeft: '5px'
                  }}>
                    {answerData.quizResult.passed ? '✅ PASSED' : '❌ FAILED'}
                  </span>
                </div>
              )}
            </div>

            {answerData.quizResult && (
              <div style={{ 
                marginTop: '15px', 
                padding: '10px', 
                backgroundColor: answerData.quizResult.passed ? '#d4edda' : '#f8d7da',
                borderRadius: '4px',
                textAlign: 'center'
              }}>
                <strong>
                  {answerData.quizResult.passed 
                    ? '🎉 Congratulations! You passed this tournament!' 
                    : `You need ${answerData.tournament.minPassingScore || 70}% to pass. Keep practicing!`
                  }
                </strong>
              </div>
            )}
          </div>

          {/* Detailed Answers */}
          <div style={{ maxHeight: '60vh', overflowY: 'auto' }}>
            <h4 style={{ marginBottom: '20px', color: '#333' }}>
              📝 Question-by-Question Review
            </h4>

            {answerData.answers.map((answer, index) => (
              <div key={index} style={{ 
                marginBottom: '25px', 
                padding: '20px', 
                border: '1px solid #e9ecef', 
                borderRadius: '8px',
                backgroundColor: '#fff',
                boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
              }}>
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center',
                  marginBottom: '15px'
                }}>
                  <strong style={{ color: '#495057', fontSize: '16px' }}>
                    Question {answer.questionNumber}
                  </strong>
                  <span style={{
                    padding: '4px 12px',
                    borderRadius: '20px',
                    fontSize: '14px',
                    fontWeight: 'bold',
                    backgroundColor: answer.isCorrect ? '#28a745' : '#dc3545',
                    color: 'white'
                  }}>
                    {answer.isCorrect ? '✓ Correct' : '✗ Incorrect'}
                  </span>
                </div>

                <div style={{ 
                  fontSize: '15px', 
                  marginBottom: '15px',
                  lineHeight: '1.5',
                  color: '#333',
                  fontWeight: '500'
                }}>
                  {answer.questionText}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' }}>
                  <div>
                    <strong style={{ 
                      color: answer.isCorrect ? '#28a745' : '#dc3545',
                      marginBottom: '8px', 
                      display: 'block',
                      fontSize: '14px'
                    }}>
                      {answer.isCorrect ? '✓' : '✗'} Your Answer:
                    </strong>
                    <div style={getAnswerStyle(answer.isCorrect)}>
                      {answer.userAnswer}
                    </div>
                  </div>

                  <div>
                    <strong style={{ 
                      color: '#28a745', 
                      marginBottom: '8px', 
                      display: 'block',
                      fontSize: '14px'
                    }}>
                      ✓ Correct Answer:
                    </strong>
                    <div style={getAnswerStyle(true)}>
                      {answer.correctAnswer}
                    </div>
                  </div>
                </div>

                <div style={{ 
                  marginTop: '12px', 
                  fontSize: '12px', 
                  color: '#666',
                  textAlign: 'right'
                }}>
                  Answered: {new Date(answer.answeredAt).toLocaleString()}
                </div>
              </div>
            ))}
          </div>

          {/* Performance Tips */}
          {answerData.quizResult && !answerData.quizResult.passed && (
            <div style={{ 
              marginTop: '20px', 
              padding: '15px', 
              backgroundColor: '#fff3cd', 
              borderRadius: '6px',
              border: '1px solid #ffeaa7'
            }}>
              <h5 style={{ margin: '0 0 10px 0', color: '#856404' }}>
                💡 Tips for Improvement:
              </h5>
              <ul style={{ margin: '0', paddingLeft: '20px', color: '#856404' }}>
                <li>Review the questions you got wrong</li>
                <li>Study more about {answerData.tournament.category}</li>
                <li>Try other {answerData.tournament.difficulty} level tournaments</li>
                <li>Practice makes perfect - keep taking quizzes!</li>
              </ul>
            </div>
          )}
        </div>
      )}

      <div style={{ marginTop: '25px', textAlign: 'right' }}>
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

export default MyAnswersModal;