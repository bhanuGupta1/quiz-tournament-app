import React, { useState, useEffect } from 'react';
import Modal from './Modal';
import api from '../services/api';

const TournamentModal = ({ 
  isOpen, 
  onClose, 
  onSuccess, 
  tournament = null, // null for create, tournament object for update
  mode = 'create' // 'create' or 'update'
}) => {
  const [formData, setFormData] = useState({
    creator: '',
    name: '',
    category: '',
    difficulty: '',
    startDate: '',
    endDate: '',
    minPassingScore: 70
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Initialize form data when modal opens or tournament changes
  useEffect(() => {
    if (isOpen) {
      if (mode === 'update' && tournament) {
        setFormData({
          creator: tournament.createdBy?.username || '',
          name: tournament.name || '',
          category: tournament.category || '',
          difficulty: tournament.difficulty || '',
          startDate: tournament.startDate ? tournament.startDate.split('T')[0] : '',
          endDate: tournament.endDate ? tournament.endDate.split('T')[0] : '',
          minPassingScore: tournament.minPassingScore || 70
        });
      } else {
        // Reset form for create mode
        setFormData({
          creator: '',
          name: '',
          category: '',
          difficulty: '',
          startDate: '',
          endDate: '',
          minPassingScore: 70
        });
      }
      setErrors({});
    }
  }, [isOpen, tournament, mode]);

  const validateForm = () => {
    const newErrors = {};

    // Creator validation - Assessment Requirement
    if (!formData.creator.trim()) {
      newErrors.creator = 'Creator field is required and cannot be blank';
    }

    // Name validation
    if (!formData.name.trim()) {
      newErrors.name = 'Tournament name is required';
    } else if (formData.name.length > 100) {
      newErrors.name = 'Tournament name must be less than 100 characters';
    }

    // Category validation
    if (!formData.category) {
      newErrors.category = 'Please select a category';
    }

    // Difficulty validation
    if (!formData.difficulty) {
      newErrors.difficulty = 'Please select a difficulty level';
    }

    // Date validation
    if (!formData.startDate) {
      newErrors.startDate = 'Start date is required';
    }

    if (!formData.endDate) {
      newErrors.endDate = 'End date is required';
    }

    if (formData.startDate && formData.endDate) {
      if (new Date(formData.startDate) >= new Date(formData.endDate)) {
        newErrors.endDate = 'End date must be after start date';
      }
    }

    // Min passing score validation
    if (formData.minPassingScore < 0 || formData.minPassingScore > 100) {
      newErrors.minPassingScore = 'Minimum passing score must be between 0 and 100';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'minPassingScore' ? parseFloat(value) || 0 : value
    }));

    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      let response;
      if (mode === 'create') {
        response = await api.post('/api/tournaments', formData);
      } else {
        response = await api.put(`/api/tournaments/${tournament.id}`, formData);
      }

      if (response.data.success) {
        onSuccess(response.data.tournament);
        onClose();
      } else {
        setErrors({ submit: response.data.error || `Failed to ${mode} tournament` });
      }
    } catch (error) {
      if (error.response?.status === 401) {
        setErrors({ submit: 'You are not authorized. Please login as admin.' });
      } else if (error.response?.status === 403) {
        setErrors({ submit: 'Access denied. Admin privileges required.' });
      } else {
        setErrors({ 
          submit: error.response?.data?.error || 
                  error.response?.data?.message || 
                  `Failed to ${mode} tournament` 
        });
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
    <Modal 
      isOpen={isOpen} 
      onClose={onClose} 
      title={mode === 'create' ? 'Create New Tournament' : 'Update Tournament'}
      size="medium"
    >
      <form onSubmit={handleSubmit}>
        {/* Global Error Message */}
        {errors.submit && (
          <div style={{ 
            background: '#f8d7da', 
            color: '#721c24', 
            padding: '12px', 
            borderRadius: '4px', 
            marginBottom: '20px' 
          }}>
            {errors.submit}
          </div>
        )}

        {/* Creator Field - Assessment Requirement */}
        <div className="form-group">
          <label htmlFor="creator">Creator *</label>
          <input
            type="text"
            id="creator"
            name="creator"
            value={formData.creator}
            onChange={handleChange}
            placeholder="Enter creator name"
            className={errors.creator ? 'error' : ''}
          />
          {errors.creator && (
            <div className="error-message">{errors.creator}</div>
          )}
        </div>

        {/* Tournament Name */}
        <div className="form-group">
          <label htmlFor="name">Tournament Name *</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            maxLength="100"
            placeholder="Enter tournament name"
            className={errors.name ? 'error' : ''}
          />
          {errors.name && (
            <div className="error-message">{errors.name}</div>
          )}
        </div>

        {/* Category */}
        <div className="form-group">
          <label htmlFor="category">Category *</label>
          <select
            id="category"
            name="category"
            value={formData.category}
            onChange={handleChange}
            className={errors.category ? 'error' : ''}
          >
            <option value="">Select a category</option>
            {categories.map(category => (
              <option key={category} value={category}>{category}</option>
            ))}
          </select>
          {errors.category && (
            <div className="error-message">{errors.category}</div>
          )}
        </div>

        {/* Difficulty */}
        <div className="form-group">
          <label htmlFor="difficulty">Difficulty *</label>
          <select
            id="difficulty"
            name="difficulty"
            value={formData.difficulty}
            onChange={handleChange}
            className={errors.difficulty ? 'error' : ''}
          >
            <option value="">Select difficulty</option>
            {difficulties.map(difficulty => (
              <option key={difficulty} value={difficulty}>
                {difficulty.charAt(0).toUpperCase() + difficulty.slice(1)}
              </option>
            ))}
          </select>
          {errors.difficulty && (
            <div className="error-message">{errors.difficulty}</div>
          )}
        </div>

        {/* Date Fields */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
          <div className="form-group">
            <label htmlFor="startDate">Start Date *</label>
            <input
              type="date"
              id="startDate"
              name="startDate"
              value={formData.startDate}
              onChange={handleChange}
              className={errors.startDate ? 'error' : ''}
            />
            {errors.startDate && (
              <div className="error-message">{errors.startDate}</div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="endDate">End Date *</label>
            <input
              type="date"
              id="endDate"
              name="endDate"
              value={formData.endDate}
              onChange={handleChange}
              min={formData.startDate}
              className={errors.endDate ? 'error' : ''}
            />
            {errors.endDate && (
              <div className="error-message">{errors.endDate}</div>
            )}
          </div>
        </div>

        {/* Min Passing Score */}
        <div className="form-group">
          <label htmlFor="minPassingScore">Minimum Passing Score (%) *</label>
          <input
            type="number"
            id="minPassingScore"
            name="minPassingScore"
            value={formData.minPassingScore}
            onChange={handleChange}
            min="0"
            max="100"
            placeholder="70"
            className={errors.minPassingScore ? 'error' : ''}
          />
          {errors.minPassingScore && (
            <div className="error-message">{errors.minPassingScore}</div>
          )}
          <small style={{ color: '#666', fontSize: '14px' }}>
            Participants need this score to pass the tournament
          </small>
        </div>

        {/* Form Actions */}
        <div style={{ display: 'flex', gap: '15px', marginTop: '30px' }}>
          <button 
            type="button" 
            onClick={onClose}
            className="btn btn-secondary"
            style={{ flex: 1 }}
            disabled={loading}
          >
            Cancel
          </button>
          <button 
            type="submit" 
            className="btn btn-primary"
            style={{ flex: 1 }}
            disabled={loading}
          >
            {loading ? 
              (mode === 'create' ? 'Creating...' : 'Updating...') : 
              (mode === 'create' ? 'Create Tournament' : 'Update Tournament')
            }
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default TournamentModal;