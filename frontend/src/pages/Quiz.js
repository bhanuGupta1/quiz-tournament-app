import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const Quiz = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [tournament, setTournament] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState('');
  const [timeLeft, setTimeLeft] = useState(30);
  const [quizCompleted, setQuizCompleted] = useState(false);
  const [quizResults, setQuizResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submittingAnswer, setSubmittingAnswer] = useState(false);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (user.role !== 'PLAYER') {
      navigate('/dashboard');
      return;
    }
    startQuiz();
  }, [id, user, navigate]);

  useEffect(() => {
    if (questions.length > 0 && !quizCompleted && timeLeft > 0) {
      const timer = setTimeout(() => {
        setTimeLeft(timeLeft - 1);
      }, 1000);
      return () => clearTimeout(timer);
    } else if (timeLeft === 0 && !quizCompleted && questions.length > 0) {
      handleNextQuestion();
    }
  }, [timeLeft, questions, quizCompleted]);

  const startQuiz = async () => {
    try {
      // Get tournament details first
      const tournamentResponse = await api.get(`/api/tournaments/${id}`);
      setTournament(tournamentResponse.data.tournament);

      // Get all questions for the tournament (OpenTDB powered)
      const questionsResponse = await api.get(`/api/tournaments/${id}/questions`);
      const questions = questionsResponse.data.questions || [];
      
      if (questions.length === 0) {
        throw new Error('No questions available for this tournament. The OpenTDB API might be down or the tournament has no questions configured.');
      }
      
      setQuestions(questions);
      setTimeLeft(30);
    } catch (error) {
      if (error.response?.status === 403) {
        setError('You are not authorized to take this quiz. Please login as a player.');
      } else if (error.response?.status === 404) {
        setError('Tournament not found or no questions available.');
      } else {
        setError('Failed to load quiz questions. The OpenTDB API might be temporarily unavailable.');
      }
      // Error details available in development mode
    } finally {
      setLoading(false);
    }
  };

  const handleAnswerSelect = (answer) => {
    setSelectedAnswer(answer);
  };

  const handleNextQuestion = async () => {
    if (!selectedAnswer && timeLeft > 0) {
      return; // Don't proceed without an answer unless time is up
    }

    setSubmittingAnswer(true);
    
    try {
      // Submit the answer to the backend
      const answerToSubmit = selectedAnswer || ''; // Empty string if time ran out
      await api.post(`/api/tournaments/${id}/questions/${currentQuestion + 1}/answer`, {
        answer: answerToSubmit
      });

      // Move to next question or complete quiz
      if (currentQuestion + 1 < questions.length) {
        setCurrentQuestion(currentQuestion + 1);
        setSelectedAnswer('');
        setTimeLeft(30);
      } else {
        await completeQuiz();
      }
    } catch (error) {
      setError('Failed to submit answer. Please try again.');
      // Error details available in development mode
    } finally {
      setSubmittingAnswer(false);
    }
  };

  const completeQuiz = async () => {
    try {
      // Complete the quiz and get final results
      const response = await api.post(`/api/tournaments/${id}/complete`);
      
      // Validate response data
      if (!response.data) {
        throw new Error('No response data received');
      }
      
      // Response logged for debugging
      setQuizResults(response.data);
      setQuizCompleted(true);
    } catch (error) {
      // Error details available in development mode
      
      // Create a fallback result if the API fails
      const fallbackResult = {
        score: 0,
        totalQuestions: questions.length,
        percentage: 0,
        passed: false,
        message: 'Quiz completed but unable to retrieve detailed results. Please check with an administrator.',
        answerHistory: []
      };
      
      setQuizResults(fallbackResult);
      setQuizCompleted(true);
      setError('Warning: Quiz completed but there was an issue retrieving your detailed results.');
    }
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="loading-spinner" style={{ margin: '0 auto 20px' }}></div>
          <h3>Loading Quiz Questions...</h3>
          <p>Fetching questions from OpenTDB API</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="card">
          <h2>Error Loading Quiz</h2>
          <p>{error}</p>
          <div style={{ display: 'flex', gap: '15px', marginTop: '20px', flexWrap: 'wrap' }}>
            <button 
              onClick={() => {
                setError('');
                setLoading(true);
                startQuiz();
              }} 
              className="btn btn-primary"
            >
              Try Again
            </button>
            <button 
              onClick={async () => {
                try {
                  const response = await api.get(`/api/tournaments/questions/health`);
                  alert(`API Health: ${JSON.stringify(response.data, null, 2)}`);
                } catch (err) {
                  alert(`API Error: ${err.message}`);
                }
              }} 
              className="btn btn-info"
              style={{ backgroundColor: '#17a2b8', color: 'white', border: '1px solid #17a2b8' }}
            >
              Test API
            </button>
            <button onClick={() => navigate('/dashboard')} className="btn btn-secondary">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!tournament && !loading) {
    return (
      <div className="container">
        <div className="card">
          <h2>Tournament not found</h2>
          <button onClick={() => navigate('/dashboard')} className="btn btn-primary">
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  if (quizCompleted) {
    // Handle case where quiz is completed but no results yet
    if (!quizResults) {
      return (
        <div className="container">
          <div style={{ textAlign: 'center', padding: '50px' }}>
            <div className="loading-spinner" style={{ margin: '0 auto 20px' }}></div>
            <h3>Processing Your Results...</h3>
            <p>Please wait while we calculate your final score.</p>
          </div>
        </div>
      );
    }
    // Ensure we have valid score data
    const score = quizResults.score || 0;
    const totalQuestions = quizResults.totalQuestions || questions.length || 0;
    const percentage = quizResults.percentage || (totalQuestions > 0 ? Math.round((score / totalQuestions) * 100) : 0);
    const passed = quizResults.passed !== undefined ? quizResults.passed : false;
    
    return (
      <div className="container">
        <div className="results">
          <h2>🎯 Quiz Completed!</h2>
          <div className="score">{percentage}%</div>
          <p>
            You scored <strong>{score}/{totalQuestions}</strong> correct answers
          </p>
          
          {/* Score breakdown */}
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', 
            gap: '15px', 
            margin: '20px 0' 
          }}>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Score</strong>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#007bff' }}>
                {score}/{totalQuestions}
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Percentage</strong>
              <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#28a745' }}>
                {percentage}%
              </div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: '#f8f9fa', borderRadius: '6px' }}>
              <strong>Status</strong>
              <div style={{ 
                fontSize: '1.2rem', 
                fontWeight: 'bold', 
                color: passed ? '#28a745' : '#dc3545' 
              }}>
                {passed ? 'PASSED' : 'FAILED'}
              </div>
            </div>
          </div>
          
          <div style={{ 
            padding: '20px', 
            background: passed ? '#d4edda' : '#f8d7da',
            color: passed ? '#155724' : '#721c24',
            borderRadius: '8px',
            margin: '20px 0'
          }}>
            <strong>{passed ? '🎉 Congratulations!' : '📚 Keep Learning!'}</strong>
            <br />
            {quizResults.message || (passed ? 'You passed the quiz!' : 'Better luck next time!')}
          </div>
          
          {/* Answer Review */}
          {quizResults.answerHistory && quizResults.answerHistory.length > 0 && (
            <div style={{ textAlign: 'left', marginTop: '30px' }}>
              <h3>📝 Review Your Answers:</h3>
              <div style={{ maxHeight: '300px', overflowY: 'auto', marginTop: '15px' }}>
                {quizResults.answerHistory.map((answer, index) => (
                  <div key={index} style={{ 
                    padding: '15px', 
                    margin: '8px 0', 
                    background: answer.correct ? '#d4edda' : '#f8d7da',
                    borderRadius: '6px',
                    border: `2px solid ${answer.correct ? '#c3e6cb' : '#f5c6cb'}`
                  }}>
                    <div style={{ marginBottom: '8px' }}>
                      <strong>Q{index + 1}:</strong> 
                      <span dangerouslySetInnerHTML={{ 
                        __html: questions[index]?.question || 'Question not available' 
                      }} />
                    </div>
                    <div style={{ fontSize: '14px' }}>
                      <span style={{ 
                        color: answer.correct ? '#155724' : '#721c24',
                        fontWeight: 'bold'
                      }}>
                        {answer.correct ? '✅' : '❌'} Your answer: 
                      </span>
                      <span dangerouslySetInnerHTML={{ 
                        __html: answer.userAnswer || '<em>No answer provided</em>' 
                      }} />
                      {!answer.correct && answer.correctAnswer && (
                        <div style={{ marginTop: '5px' }}>
                          <span style={{ color: '#155724', fontWeight: 'bold' }}>
                            ✓ Correct answer: 
                          </span>
                          <span dangerouslySetInnerHTML={{ __html: answer.correctAnswer }} />
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div style={{ display: 'flex', gap: '15px', justifyContent: 'center', marginTop: '30px' }}>
            <button onClick={() => navigate('/dashboard')} className="btn btn-primary">
              Back to Dashboard
            </button>
            <button onClick={() => navigate(`/tournament/${id}`)} className="btn btn-secondary">
              Tournament Details
            </button>
            <button onClick={() => navigate(`/leaderboard/${id}`)} className="btn btn-secondary">
              View Leaderboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  const question = questions[currentQuestion];
  const totalQuestions = questions.length;
  const progress = totalQuestions > 0 ? ((currentQuestion + 1) / totalQuestions) * 100 : 0;

  // Debug info available in development mode only

  // Show loading if no questions yet
  if (questions.length === 0 && !error) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="loading-spinner" style={{ margin: '0 auto 20px' }}></div>
          <h3>Loading Quiz Questions...</h3>
          <p>Fetching questions from OpenTDB API</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="question-card">
        <div className="question-header">
          <div className="question-number">
            Question {currentQuestion + 1} of {totalQuestions}
          </div>
          <div className="timer" style={{ 
            color: timeLeft <= 10 ? '#dc3545' : timeLeft <= 20 ? '#ffc107' : '#28a745' 
          }}>
            {timeLeft}s
          </div>
        </div>

        {tournament && (
          <div style={{ 
            background: '#f8f9fa', 
            padding: '10px', 
            borderRadius: '4px', 
            marginBottom: '20px',
            fontSize: '14px'
          }}>
            <strong>{tournament.name}</strong> | {tournament.category} | {tournament.difficulty?.charAt(0).toUpperCase() + tournament.difficulty?.slice(1)}
          </div>
        )}

        <div style={{ marginBottom: '20px' }}>
          <div style={{ fontSize: '14px', color: '#666', marginBottom: '10px' }}>
            Category: {question?.category} | Difficulty: {question?.difficulty?.charAt(0).toUpperCase() + question?.difficulty?.slice(1)}
          </div>
          <h2 className="question-text" dangerouslySetInnerHTML={{ 
            __html: question?.question || 'Loading question...' 
          }} />
        </div>

        <div className="options">
          {(() => {
            const options = question?.answerOptions || question?.answers || [];
            
            if (options.length === 0) {
              return (
                <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
                  {question ? (
                    <div>
                      <p>No answer options available for this question.</p>
                      {process.env.NODE_ENV === 'development' && (
                        <details style={{ marginTop: '10px', fontSize: '12px' }}>
                          <summary>Debug Info</summary>
                          <pre>{JSON.stringify(question, null, 2)}</pre>
                        </details>
                      )}
                    </div>
                  ) : (
                    'Loading options...'
                  )}
                </div>
              );
            }
            
            return options.map((option, index) => (
              <div
                key={index}
                className={`option ${selectedAnswer === option ? 'selected' : ''}`}
                onClick={() => handleAnswerSelect(option)}
                dangerouslySetInnerHTML={{ __html: option }}
              />
            ));
          })()}
        </div>

        <div className="quiz-controls">
          <button 
            onClick={() => navigate('/dashboard')} 
            className="btn btn-secondary"
          >
            Quit Tournament
          </button>
          
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ width: `${progress}%` }}
            ></div>
          </div>
          
          <button 
            onClick={handleNextQuestion}
            className="btn btn-primary"
            disabled={submittingAnswer || (!selectedAnswer && timeLeft > 0)}
          >
            {submittingAnswer ? 'Submitting...' : 
             currentQuestion + 1 === totalQuestions ? 'Finish Quiz' : 'Next Question'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Quiz;