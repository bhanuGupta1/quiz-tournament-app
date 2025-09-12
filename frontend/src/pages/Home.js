import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Home = () => {
  const { user } = useAuth();

  return (
    <div>
      <section className="hero">
        <div className="container">
          <h1>Quiz Tournament</h1>
          <p>Test your knowledge and compete with players from around the world</p>
          {user ? (
            <Link to="/dashboard" className="btn btn-primary">
              Go to Dashboard
            </Link>
          ) : (
            <div style={{ display: 'flex', gap: '15px', justifyContent: 'center' }}>
              <Link to="/register" className="btn btn-primary">
                Get Started
              </Link>
              <Link to="/login" className="btn btn-secondary">
                Login
              </Link>
            </div>
          )}
        </div>
      </section>

      <section className="features">
        <div className="container">
          <h2 style={{ textAlign: 'center', marginBottom: '20px' }}>
            Why Choose Quiz Tournament?
          </h2>
          <div className="features-grid">
            <div className="feature-card">
              <h3>🏆 Competitive Gaming</h3>
              <p>
                Participate in tournaments and climb the leaderboards. 
                Compete with players worldwide and prove your knowledge.
              </p>
            </div>
            <div className="feature-card">
              <h3>📚 Diverse Topics</h3>
              <p>
                From science and history to pop culture and sports, 
                find quizzes on topics that interest you most.
              </p>
            </div>
            <div className="feature-card">
              <h3>⚡ Real-time Play</h3>
              <p>
                Experience fast-paced, real-time quiz battles with 
                instant scoring and live leaderboards.
              </p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;