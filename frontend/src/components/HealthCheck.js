import { useState } from 'react';
import api from '../services/api';

const HealthCheck = () => {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  const checkHealth = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/health');
      setStatus({
        success: true,
        data: response.data
      });
    } catch (error) {
      setStatus({
        success: false,
        error: error.message,
        details: error.response?.data || 'No response from server'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ 
      position: 'fixed', 
      bottom: '20px', 
      right: '20px', 
      background: 'white', 
      padding: '15px', 
      borderRadius: '8px',
      boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
      border: '1px solid #ddd',
      maxWidth: '300px'
    }}>
      <h4 style={{ margin: '0 0 10px 0' }}>Backend Health</h4>
      
      <button 
        onClick={checkHealth} 
        disabled={loading}
        style={{
          padding: '8px 16px',
          background: '#007bff',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: loading ? 'not-allowed' : 'pointer'
        }}
      >
        {loading ? 'Checking...' : 'Check Status'}
      </button>

      {status && (
        <div style={{ marginTop: '10px', fontSize: '14px' }}>
          {status.success ? (
            <div style={{ color: '#28a745' }}>
              ✅ Backend is running
              <br />
              Status: {status.data.status}
              <br />
              Version: {status.data.version}
            </div>
          ) : (
            <div style={{ color: '#dc3545' }}>
              ❌ Backend connection failed
              <br />
              Error: {status.error}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default HealthCheck;