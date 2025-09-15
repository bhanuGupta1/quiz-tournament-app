import React, { useState, useEffect } from 'react';
import Modal from './Modal';
import api from '../services/api';

const TournamentQuestionsModal = ({ 
  isOpen, 
  onClose, 
  tournament 
}) => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen && tournament) {
      fetchQuestions();
    }
  }, [isOpen, tournament]);

  const fetchQuestions = async () => {
    setLoading(true);
    setError('');
    
    try {
      const response = await api.get(`/api/tournaments/${tournament.id}/questions/admin`);
      
      if (response.data.success) {
        setQuestions(response.data.questions || []);
      } else {
        setError(response.data.error || 'Failed to load questions');
      }
    } catch (error) {
      if (error.response?.status === 401) {
        setError('You are not authorized to view questions. Please login as admin.');
      } else if (error.response?.status === 403) {
        setError('Access denied. Admin privileges required.');
      } else {
        setError(error.response?.data?.error || 'Failed to load tournament questions');
      }
    } finally {
      setLoading(false);
    }
  };

  const getAnswerStyle = (answer, correctAnswer) => {
    return {
      padding: '8px 12px',
      margin: '4px 0',
      borderRadius: '4px',
      backgroundColor: answer === correctAnswer ? '#d4edda' : '#f8f9fa',
      border: answer === correctAnswer ? '1px solid #c3e6cb' : '1px solid #e9ecef',
      color: answer === correctAnswer ? '#155724' : '#495057'
    };
  };

  return (
    <Modal 
      isOpen={isOpen} 
      onClose={onClose} 
      title={`Questions: ${tournament?.name || 'Tournament'}`}
      size="large"
    >
      {loading && (
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <div>Loading questions...</div>
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

      {!loading && !error && questions.length === 0 && (
        <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
          <p>No questions available for this tournament.</p>
          <p>Questions are generated when a player starts the quiz.</p>
        </div>
      )}

      {!loading && !error && questions.length > 0 && (
        <div>
          <div style={{ marginBottom: '20px', padding: '12px', backgroundColor: '#e7f3ff', borderRadius: '4px' }}>
            <strong>Tournament Details:</strong><br />
            Category: {tournament?.category} | Difficulty: {tournament?.difficulty} | Total Questions: {questions.length}
          </div>

          <div style={{ maxHeight: '60vh', overflowY: 'auto' }}>
            {questions.map((question, index) => (
              <div key={index} style={{ 
                marginBottom: '30px', 
                padding: '20px', 
                border: '1px solid #e9ecef', 
                borderRadius: '8px',
                backgroundColor: '#fff'
              }}>
                <div style={{ marginBottom: '15px' }}>
                  <strong style={{ color: '#007bff' }}>
                    Question {question.questionNumber || index + 1} of {question.totalQuestions || questions.length}
                  </strong>
                  <span style={{ 
                    marginLeft: '10px', 
                    padding: '2px 8px', 
                    backgroundColor: '#f8f9fa', 
                    borderRadius: '12px', 
                    fontSize: '12px',
                    color: '#666'
                  }}>
                    {question.type === 'boolean' ? 'True/False' : 'Multiple Choice'}
                  </span>
                </div>

                <div style={{ 
                  fontSize: '16px', 
                  fontWeight: '500', 
                  marginBottom: '15px',
                  lineHeight: '1.5'
                }}>
                  {question.question}
                </div>

                <div style={{ marginBottom: '15px' }}>
                  <strong style={{ color: '#28a745', marginBottom: '8px', display: 'block' }}>
                    ✓ Correct Answer:
                  </strong>
                  <div style={getAnswerStyle(question.correctAnswer, question.correctAnswer)}>
                    {question.correctAnswer}
                  </div>
                </div>

                {question.incorrectAnswers && question.incorrectAnswers.length > 0 && (
                  <div>
                    <strong style={{ color: '#dc3545', marginBottom: '8px', display: 'block' }}>
                      ✗ Incorrect Answers:
                    </strong>
                    {question.incorrectAnswers.map((answer, answerIndex) => (
                      <div key={answerIndex} style={getAnswerStyle(answer, question.correctAnswer)}>
                        {answer}
                      </div>
                    ))}
                  </div>
                )}

                <div style={{ 
                  marginTop: '15px', 
                  padding: '8px 0', 
                  borderTop: '1px solid #e9ecef',
                  fontSize: '14px',
                  color: '#666'
                }}>
                  Category: {question.category} | Difficulty: {question.difficulty}
                </div>
              </div>
            ))}
          </div>

          <div style={{ 
            marginTop: '20px', 
            padding: '15px', 
            backgroundColor: '#f8f9fa', 
            borderRadius: '4px',
            textAlign: 'center'
          }}>
            <strong>Total Questions: {questions.length}</strong>
            <br />
            <small style={{ color: '#666' }}>
              Questions are sourced from OpenTDB and cached for consistent gameplay
            </small>
          </div>
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

export default TournamentQuestionsModal;