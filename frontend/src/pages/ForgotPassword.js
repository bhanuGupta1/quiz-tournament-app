import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const response = await api.post('/api/auth/forgot-password', { email });
      
      if (response.data.success) {
        setMessage('Password reset instructions have been sent to your email address.');
      } else {
        setError(response.data.error || 'Failed to send password reset email');
      }
    } catch (error) {
      if (error.response?.status === 404) {
        setError('No account found with that email address.');
      } else {
        setError(error.response?.data?.error || 'Failed to send password reset email');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container" style={{ maxWidth: '400px', marginTop: '50px' }}>
      <div className="card">
        <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>Forgot Password</h2>
        
        <p style={{ textAlign: 'center', color: '#666', marginBottom: '30px' }}>
          Enter your email address and we'll send you instructions to reset your password.
        </p>

        {error && (
          <div style={{ 
            background: '#f8d7da', 
            color: '#721c24', 
            padding: '12px', 
            borderRadius: '4px', 
            marginBottom: '20px',
            textAlign: 'center'
          }}>
            {error}
          </div>
        )}

        {message && (
          <div style={{ 
            background: '#d4edda', 
            color: '#155724', 
            padding: '12px', 
            borderRadius: '4px', 
            marginBottom: '20px',
            textAlign: 'center'
          }}>
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              name="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="Enter your email address"
              disabled={loading}
            />
          </div>

          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%' }}
            disabled={loading || !email}
          >
            {loading ? 'Sending...' : 'Send Reset Instructions'}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '20px' }}>
          <Link to="/login">Back to Login</Link>
        </div>

        {/* Note about email functionality */}
        <div style={{ 
          marginTop: '20px', 
          padding: '10px', 
          backgroundColor: '#fff3cd', 
          borderRadius: '4px',
          fontSize: '14px',
          textAlign: 'center'
        }}>
          <strong>Note:</strong> Email functionality is disabled for testing. 
          In a production environment, you would receive an email with reset instructions.
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;