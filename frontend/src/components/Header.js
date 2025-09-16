import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Header.css';

const Header = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="container">
        <div className="header-content">
          <Link to="/" className="logo">
            <h2>Quiz Tournament</h2>
          </Link>
          
          <nav className="nav">
            {user ? (
              <>
                <Link to="/dashboard" className="nav-link">Dashboard</Link>
                {user.role === 'PLAYER' && (
                  <Link to="/popular-tournaments" className="nav-link">🏆 Popular</Link>
                )}
                {user.role === 'ADMIN' && (
                  <Link to="/admin" className="nav-link">Admin Panel</Link>
                )}
                <Link to="/profile" className="nav-link">Profile</Link>
                <span className="user-info">Welcome, {user.username}</span>
                <button onClick={handleLogout} className="btn btn-secondary">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="nav-link">Login</Link>
                <Link to="/register" className="btn btn-primary">
                  Sign Up
                </Link>
              </>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
};

export default Header;