import React, { useState, useEffect } from 'react';
import Modal from './Modal';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const ProfileUpdateModal = ({ 
  isOpen, 
  onClose, 
  onSuccess 
}) => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    city: '',
    preferredCategory: ''
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Initialize form data when modal opens
  useEffect(() => {
    if (isOpen && user) {
      setFormData({
        username: user.username || '',
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        phoneNumber: user.phoneNumber || '',
        city: user.city || '',
        preferredCategory: user.preferredCategory || ''
      });
      setErrors({});
    }
  }, [isOpen, user]);

  const validateForm = () => {
    const newErrors = {};

    // Username validation
    if (!formData.username.trim()) {
      newErrors.username = 'Username is required';
    } else if (formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }

    // First name validation
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'First name is required';
    }

    // Last name validation
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Last name is required';
    }

    // Email validation
    if (!formData.email.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email is invalid';
    }

    // Phone number validation (optional but if provided, should be valid)
    if (formData.phoneNumber && formData.phoneNumber.length > 20) {
      newErrors.phoneNumber = 'Phone number cannot exceed 20 characters';
    }

    // City validation (optional but if provided, should be valid)
    if (formData.city && formData.city.length > 100) {
      newErrors.city = 'City cannot exceed 100 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
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
      const response = await api.put('/api/auth/profile', formData);

      if (response.data.success) {
        onSuccess(response.data.user);
        onClose();
      } else {
        setErrors({ submit: response.data.error || 'Failed to update profile' });
      }
    } catch (error) {
      if (error.response?.status === 401) {
        setErrors({ submit: 'You are not authorized. Please login again.' });
      } else {
        setErrors({ 
          submit: error.response?.data?.error || 
                  error.response?.data?.message || 
                  'Failed to update profile' 
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

  return (
    <Modal 
      isOpen={isOpen} 
      onClose={onClose} 
      title="Update Profile"
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

        {/* Username */}
        <div className="form-group">
          <label htmlFor="username">Username *</label>
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="Enter username"
            className={errors.username ? 'error' : ''}
          />
          {errors.username && (
            <div className="error-message">{errors.username}</div>
          )}
        </div>

        {/* First Name */}
        <div className="form-group">
          <label htmlFor="firstName">First Name *</label>
          <input
            type="text"
            id="firstName"
            name="firstName"
            value={formData.firstName}
            onChange={handleChange}
            placeholder="Enter first name"
            className={errors.firstName ? 'error' : ''}
          />
          {errors.firstName && (
            <div className="error-message">{errors.firstName}</div>
          )}
        </div>

        {/* Last Name */}
        <div className="form-group">
          <label htmlFor="lastName">Last Name *</label>
          <input
            type="text"
            id="lastName"
            name="lastName"
            value={formData.lastName}
            onChange={handleChange}
            placeholder="Enter last name"
            className={errors.lastName ? 'error' : ''}
          />
          {errors.lastName && (
            <div className="error-message">{errors.lastName}</div>
          )}
        </div>

        {/* Email */}
        <div className="form-group">
          <label htmlFor="email">Email *</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="Enter email address"
            className={errors.email ? 'error' : ''}
          />
          {errors.email && (
            <div className="error-message">{errors.email}</div>
          )}
        </div>

        {/* Phone Number (Additional Attribute 1) */}
        <div className="form-group">
          <label htmlFor="phoneNumber">Phone Number</label>
          <input
            type="tel"
            id="phoneNumber"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={handleChange}
            placeholder="Enter phone number (optional)"
            className={errors.phoneNumber ? 'error' : ''}
          />
          {errors.phoneNumber && (
            <div className="error-message">{errors.phoneNumber}</div>
          )}
        </div>

        {/* City (Additional Attribute 2) */}
        <div className="form-group">
          <label htmlFor="city">City</label>
          <input
            type="text"
            id="city"
            name="city"
            value={formData.city}
            onChange={handleChange}
            placeholder="Enter your city (optional)"
            className={errors.city ? 'error' : ''}
          />
          {errors.city && (
            <div className="error-message">{errors.city}</div>
          )}
        </div>

        {/* Preferred Category (Additional Attribute 3) */}
        <div className="form-group">
          <label htmlFor="preferredCategory">Preferred Quiz Category</label>
          <select
            id="preferredCategory"
            name="preferredCategory"
            value={formData.preferredCategory}
            onChange={handleChange}
          >
            <option value="">Select preferred category (optional)</option>
            {categories.map(category => (
              <option key={category} value={category}>{category}</option>
            ))}
          </select>
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
            {loading ? 'Updating...' : 'Update Profile'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default ProfileUpdateModal;