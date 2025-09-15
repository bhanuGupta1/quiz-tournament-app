import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const result = await login(formData.username, formData.password);
    
    if (result.success) {
      // Check if user is admin and redirect accordingly
      const userData = result.user || JSON.parse(localStorage.getItem('user') || '{}');
      if (userData.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
    } else {
      setError(result.error);
    }
    
    setLoading(false);
  };

  return (
    <div className="container" style={{ maxWidth: '400px', marginTop: '50px' }}>
      <div className="card">
        <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>Login</h2>
        
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

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">Username or Email</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%' }}
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '20px' }}>
          Don't have an account? <Link to="/register">Sign up here</Link>
        </p>

        {/* Test Credentials Helper */}
        <div style={{ 
          marginTop: '20px', 
          padding: '15px', 
          backgroundColor: '#e7f3ff', 
          borderRadius: '8px',
          border: '1px solid #b3d9ff'
        }}>
          <h4 style={{ margin: '0 0 10px 0', fontSize: '14px', color: '#0066cc' }}>
            🔑 Quick Test Logins
          </h4>
          <div style={{ fontSize: '13px', color: '#333' }}>
            <div style={{ marginBottom: '8px' }}>
              <strong>Admin:</strong> 
              <button 
                type="button"
                onClick={() => setFormData({ username: 'admin', password: 'op@1234' })}
                style={{ 
                  marginLeft: '8px', 
                  padding: '2px 8px', 
                  fontSize: '12px',
                  backgroundColor: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                admin / op@1234
              </button>
            </div>
            <div style={{ marginBottom: '8px' }}>
              <strong>Player:</strong> 
              <button 
                type="button"
                onClick={() => setFormData({ username: 'user', password: 'user' })}
                style={{ 
                  marginLeft: '8px', 
                  padding: '2px 8px', 
                  fontSize: '12px',
                  backgroundColor: '#28a745',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                user / user
              </button>
            </div>
            <div>
              <strong>More Players:</strong> 
              <button 
                type="button"
                onClick={() => setFormData({ username: 'player1', password: 'password' })}
                style={{ 
                  marginLeft: '8px', 
                  padding: '2px 8px', 
                  fontSize: '12px',
                  backgroundColor: '#6c757d',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                player1 / password
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;