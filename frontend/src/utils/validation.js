// Input validation and sanitization utilities

export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePassword = (password) => {
  return password && password.length >= 6;
};

export const validateUsername = (username) => {
  const usernameRegex = /^[a-zA-Z0-9_]{3,50}$/;
  return usernameRegex.test(username);
};

export const sanitizeInput = (input) => {
  if (typeof input !== 'string') return input;
  
  return input
    .replace(/[<>]/g, '') // Remove potential HTML tags
    .trim() // Remove whitespace
    .slice(0, 1000); // Limit length
};

export const validateTournamentName = (name) => {
  return name && name.trim().length >= 3 && name.trim().length <= 100;
};

export const validateScore = (score) => {
  const numScore = Number(score);
  return !isNaN(numScore) && numScore >= 0 && numScore <= 100;
};

export const validateDate = (dateString) => {
  const date = new Date(dateString);
  return date instanceof Date && !isNaN(date);
};

export const validateRequired = (value) => {
  return value !== null && value !== undefined && value.toString().trim() !== '';
};

export default {
  validateEmail,
  validatePassword,
  validateUsername,
  sanitizeInput,
  validateTournamentName,
  validateScore,
  validateDate,
  validateRequired
};