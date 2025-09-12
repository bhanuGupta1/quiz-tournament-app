import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const CreateTournament = () => {
  const [formData, setFormData] = useState({
    name: '',
    category: '',
    difficulty: '',
    startDate: '',
    endDate: '',
    minPassingScore: 70
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const { user } = useAuth();
  const navigate = useNavigate();

  React.useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/dashboard');
    }
  }, [user, navigate]);



  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'minPassingScore' ? parseFloat(value) : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // Validation
    if (new Date(formData.startDate) >= new Date(formData.endDate)) {
      setError('End date must be after start date');
      setLoading(false);
      return;
    }

    // Allow past dates - useful for creating historical tournaments or testing

    if (formData.minPassingScore < 0 || formData.minPassingScore > 100) {
      setError('Minimum passing score must be between 0 and 100');
      setLoading(false);
      return;
    }

    try {
      const response = await api.post('/api/tournaments', formData);
      
      if (response.data.success) {
        navigate('/admin');
      } else {
        setError(response.data.error || 'Failed to create tournament');
      }
    } catch (error) {

      
      if (error.response?.status === 401) {
        setError('You are not authorized to create tournaments. Please login as admin.');
      } else if (error.response?.status === 403) {
        setError('Access denied. Admin privileges required.');
      } else {
        setError(error.response?.data?.error || error.response?.data?.message || 'Failed to create tournament');
      }
    } finally {
      setLoading(false);
    }
  };

  const categories = [
    'Science', 'History', 'Sports', 'Entertainment', 
    'Geography', 'Literature', 'Technology', 'General Knowledge',
    'Mathematics', 'Art', 'Music', 'Movies'
  ];

  const difficulties = ['easy', 'medium', 'hard'];

  return (
    <div className="container" style={{ maxWidth: '600px', marginTop: '30px' }}>
      <div className="card">
        <h2>Create New Tournament</h2>
        

        
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
            <label htmlFor="name">Tournament Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              required
              maxLength="100"
              placeholder="Enter tournament name"
            />
          </div>

          <div className="form-group">
            <label htmlFor="category">Category *</label>
            <select
              id="category"
              name="category"
              value={formData.category}
              onChange={handleChange}
              required
            >
              <option value="">Select a category</option>
              {categories.map(category => (
                <option key={category} value={category}>{category}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="difficulty">Difficulty *</label>
            <select
              id="difficulty"
              name="difficulty"
              value={formData.difficulty}
              onChange={handleChange}
              required
            >
              <option value="">Select difficulty</option>
              {difficulties.map(difficulty => (
                <option key={difficulty} value={difficulty}>
                  {difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
                </option>
              ))}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
            <div className="form-group">
              <label htmlFor="startDate">Start Date *</label>
              <input
                type="date"
                id="startDate"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                required
              />
              <small style={{ color: '#666', fontSize: '14px' }}>
                Can be past, present, or future date
              </small>
            </div>

            <div className="form-group">
              <label htmlFor="endDate">End Date *</label>
              <input
                type="date"
                id="endDate"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                required
                min={formData.startDate}
              />
              <small style={{ color: '#666', fontSize: '14px' }}>
                Must be after start date
              </small>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="minPassingScore">Minimum Passing Score (%) *</label>
            <input
              type="number"
              id="minPassingScore"
              name="minPassingScore"
              value={formData.minPassingScore}
              onChange={handleChange}
              required
              min="0"
              max="100"
              placeholder="70"
            />
            <small style={{ color: '#666', fontSize: '14px' }}>
              Participants need this score to pass the tournament
            </small>
          </div>



          <div style={{ display: 'flex', gap: '15px', marginTop: '30px' }}>
            <button 
              type="button" 
              onClick={() => navigate('/admin')}
              className="btn btn-secondary"
              style={{ flex: 1 }}
            >
              Cancel
            </button>
            <button 
              type="submit" 
              className="btn btn-primary"
              style={{ flex: 1 }}
              disabled={loading}
            >
              {loading ? 'Creating...' : 'Create Tournament'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateTournament;