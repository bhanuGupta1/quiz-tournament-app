import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const TournamentResults = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [tournament, setTournament] = useState(null);
  const [results, setResults] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (user.role !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    fetchTournamentResults();
  }, [id, user, navigate]);

  const fetchTournamentResults = async () => {
    try {
      const response = await api.get(`/api/tournaments/${id}/results`);
      console.log('Tournament results response:', response.data);
      
      setTournament(response.data.tournament);
      setResults(response.data.results || []);
      setStatistics(response.data.statistics || {});
    } catch (error) {
      console.error('Failed to fetch tournament results:', error);
      setError('Failed to load tournament results. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  const formatDuration = (seconds) => {
    if (!seconds) return 'N/A';
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}m ${remainingSeconds}s`;
  };

  if (loading) {
    return (
      <div className="container">
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="loading-spinner" style={{ margin: '0 auto 20px' }}></div>
          <h3>Loading Tournament Results...</h3>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="card">
          <h2>Error Loading Results</h2>
          <p>{error}</p>
          <div style={{ display: 'flex', gap: '15px', marginTop: '20px' }}>
            <button onClick={fetchTournamentResults} className="btn btn-primary">
              Try Again
            </button>
            <button onClick={() => navigate('/admin/tournaments')} className="btn btn-secondary">
              Back to Tournaments
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!tournament) {
    return (
      <div className="container">
        <div className="card">
          <h2>Tournament not found</h2>
          <button onClick={() => navigate('/admin/tournaments')} className="btn btn-primary">
            Back to Tournaments
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      {/* Header */}
      <div style={{ marginBottom: '30px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h1>📊 Tournament Results</h1>
          <div style={{ display: 'flex', gap: '10px' }}>
            <Link to={`/tournament/${id}`} className="btn btn-secondary">
              View Tournament
            </Link>
            <Link to="/admin/tournaments" className="btn btn-primary">
              Back to Tournaments
            </Link>
          </div>
        </div>
        
        {/* Tournament Info */}
        <div className="card" style={{ marginBottom: '20px' }}>
          <h2>{tournament.name}</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px', marginTop: '15px' }}>
            <div>
              <strong>Category:</strong> {tournament.category}
            </div>
            <div>
              <strong>Difficulty:</strong> {tournament.difficulty?.charAt(0).toUpperCase() + tournament.difficulty?.slice(1)}
            </div>
            <div>
              <strong>Status:</strong> 
              <span style={{ 
                color: tournament.status === 'ONGOING' ? '#28a745' : 
                       tournament.status === 'UPCOMING' ? '#ffc107' : '#6c757d',
                marginLeft: '5px'
              }}>
                {tournament.status}
              </span>
            </div>
            <div>
              <strong>Pass Score:</strong> {tournament.minPassingScore}%
            </div>
          </div>
        </div>
      </div>

      {/* Statistics */}
      {statistics && (
        <div className="card" style={{ marginBottom: '30px' }}>
          <h3>📈 Statistics</h3>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', 
            gap: '20px', 
            marginTop: '20px' 
          }}>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#007bff' }}>
                {statistics.totalParticipants}
              </div>
              <div>Total Participants</div>
            </div>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#28a745' }}>
                {statistics.passedCount}
              </div>
              <div>Passed</div>
            </div>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#dc3545' }}>
                {statistics.failedCount}
              </div>
              <div>Failed</div>
            </div>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ffc107' }}>
                {statistics.passRate?.toFixed(1)}%
              </div>
              <div>Pass Rate</div>
            </div>
            <div style={{ textAlign: 'center', padding: '20px', background: '#f8f9fa', borderRadius: '8px' }}>
              <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#17a2b8' }}>
                {statistics.averageScore?.toFixed(1)}%
              </div>
              <div>Average Score</div>
            </div>
          </div>
        </div>
      )}

      {/* Results Table */}
      <div className="card">
        <h3>🏆 Individual Results</h3>
        
        {results.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            <h4>No Results Yet</h4>
            <p>No players have completed this tournament yet.</p>
          </div>
        ) : (
          <div style={{ overflowX: 'auto', marginTop: '20px' }}>
            <table style={{ 
              width: '100%', 
              borderCollapse: 'collapse',
              fontSize: '14px'
            }}>
              <thead>
                <tr style={{ backgroundColor: '#f8f9fa' }}>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>
                    Rank
                  </th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '2px solid #dee2e6' }}>
                    Player
                  </th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #dee2e6' }}>
                    Score
                  </th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #dee2e6' }}>
                    Percentage
                  </th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #dee2e6' }}>
                    Status
                  </th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #dee2e6' }}>
                    Time Taken
                  </th>
                  <th style={{ padding: '12px', textAlign: 'center', borderBottom: '2px solid #dee2e6' }}>
                    Completed At
                  </th>
                </tr>
              </thead>
              <tbody>
                {results.map((result, index) => (
                  <tr key={result.id} style={{ 
                    borderBottom: '1px solid #dee2e6',
                    backgroundColor: index % 2 === 0 ? 'white' : '#f8f9fa'
                  }}>
                    <td style={{ padding: '12px', fontWeight: 'bold' }}>
                      {index === 0 && '🥇 '}
                      {index === 1 && '🥈 '}
                      {index === 2 && '🥉 '}
                      #{index + 1}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div>
                        <strong>{result.user?.firstName} {result.user?.lastName}</strong>
                        <div style={{ fontSize: '12px', color: '#666' }}>
                          {result.user?.email}
                        </div>
                      </div>
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center', fontWeight: 'bold' }}>
                      {result.score}/{result.totalQuestions}
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center' }}>
                      <span style={{ 
                        fontWeight: 'bold',
                        color: result.percentage >= tournament.minPassingScore ? '#28a745' : '#dc3545'
                      }}>
                        {result.percentage?.toFixed(1)}%
                      </span>
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center' }}>
                      <span style={{ 
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        backgroundColor: result.passed ? '#d4edda' : '#f8d7da',
                        color: result.passed ? '#155724' : '#721c24'
                      }}>
                        {result.passed ? 'PASSED' : 'FAILED'}
                      </span>
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center' }}>
                      {formatDuration(result.timeTakenSeconds)}
                    </td>
                    <td style={{ padding: '12px', textAlign: 'center', fontSize: '12px' }}>
                      {formatDate(result.completedAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Export Options */}
      {results.length > 0 && (
        <div style={{ marginTop: '20px', textAlign: 'center' }}>
          <button 
            onClick={() => {
              const csvContent = [
                ['Rank', 'Player Name', 'Email', 'Score', 'Total Questions', 'Percentage', 'Status', 'Time Taken (seconds)', 'Completed At'],
                ...results.map((result, index) => [
                  index + 1,
                  `${result.user?.firstName} ${result.user?.lastName}`,
                  result.user?.email,
                  result.score,
                  result.totalQuestions,
                  result.percentage?.toFixed(1),
                  result.passed ? 'PASSED' : 'FAILED',
                  result.timeTakenSeconds || 0,
                  result.completedAt
                ])
              ].map(row => row.join(',')).join('\n');
              
              const blob = new Blob([csvContent], { type: 'text/csv' });
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = `${tournament.name}_results.csv`;
              a.click();
              window.URL.revokeObjectURL(url);
            }}
            className="btn btn-secondary"
            style={{ marginRight: '10px' }}
          >
            📥 Export CSV
          </button>
          <button 
            onClick={() => window.print()}
            className="btn btn-secondary"
          >
            🖨️ Print Results
          </button>
        </div>
      )}
    </div>
  );
};

export default TournamentResults;