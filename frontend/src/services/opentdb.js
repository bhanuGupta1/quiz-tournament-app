// OpenTDB API service (fallback - mainly handled by backend)
const OPENTDB_BASE_URL = 'https://opentdb.com/api.php';

// Decode HTML entities
export const decodeHtml = (html) => {
  const txt = document.createElement('textarea');
  txt.innerHTML = html;
  return txt.value;
};

// Category mapping (OpenTDB category IDs)
export const CATEGORIES = {
  'General Knowledge': 9,
  'Entertainment: Books': 10,
  'Entertainment: Film': 11,
  'Entertainment: Music': 12,
  'Entertainment: Musicals & Theatres': 13,
  'Entertainment: Television': 14,
  'Entertainment: Video Games': 15,
  'Entertainment: Board Games': 16,
  'Science & Nature': 17,
  'Science: Computers': 18,
  'Science: Mathematics': 19,
  'Mythology': 20,
  'Sports': 21,
  'Geography': 22,
  'History': 23,
  'Politics': 24,
  'Art': 25,
  'Celebrities': 26,
  'Animals': 27,
  'Vehicles': 28,
  'Entertainment: Comics': 29,
  'Science: Gadgets': 30,
  'Entertainment: Japanese Anime & Manga': 31,
  'Entertainment: Cartoon & Animations': 32
};

// Difficulty levels
export const DIFFICULTIES = {
  'easy': 'easy',
  'medium': 'medium', 
  'hard': 'hard'
};

// Fetch questions from OpenTDB (fallback function)
export const fetchQuestionsFromOpenTDB = async (amount = 10, category = null, difficulty = null) => {
  try {
    let url = `${OPENTDB_BASE_URL}?amount=${amount}&type=multiple`;
    
    if (category && CATEGORIES[category]) {
      url += `&category=${CATEGORIES[category]}`;
    }
    
    if (difficulty && DIFFICULTIES[difficulty]) {
      url += `&difficulty=${difficulty}`;
    }
    
    const response = await fetch(url);
    const data = await response.json();
    
    if (data.response_code === 0) {
      return data.results.map((question, index) => ({
        id: index + 1,
        question: decodeHtml(question.question),
        category: decodeHtml(question.category),
        difficulty: question.difficulty,
        type: question.type,
        correct_answer: decodeHtml(question.correct_answer),
        incorrect_answers: question.incorrect_answers.map(decodeHtml),
        answers: shuffleArray([
          decodeHtml(question.correct_answer),
          ...question.incorrect_answers.map(decodeHtml)
        ])
      }));
    } else {
      throw new Error(`OpenTDB API Error: ${getErrorMessage(data.response_code)}`);
    }
  } catch (error) {
    console.error('OpenTDB API Error:', error);
    throw error;
  }
};

// Shuffle array utility
const shuffleArray = (array) => {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
};

// Get error message from response code
const getErrorMessage = (code) => {
  switch (code) {
    case 1: return 'No Results - Could not return results. The API doesn\'t have enough questions for your query.';
    case 2: return 'Invalid Parameter - Contains an invalid parameter. Arguments passed in aren\'t valid.';
    case 3: return 'Token Not Found - Session Token does not exist.';
    case 4: return 'Token Empty - Session Token has returned all possible questions for the specified query.';
    default: return 'Unknown Error';
  }
};

export default {
  fetchQuestionsFromOpenTDB,
  decodeHtml,
  CATEGORIES,
  DIFFICULTIES
};