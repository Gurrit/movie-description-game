/**
 * REST API Client for Movie Description Game
 * 
 * Base URL is configured via REACT_APP_API_BASE environment variable.
 * Defaults to http://localhost:8080/api for development.
 */

import axios from "axios";

// Configuration
const API_BASE = process.env.REACT_APP_API_BASE || "http://localhost:8080/api";

/**
 * Axios instance with base URL and default headers
 */
const api = axios.create({
  baseURL: API_BASE,
  headers: {
    "Content-Type": "application/json",
  },
});

// ============================================================================
// SESSION ENDPOINTS
// ============================================================================

/**
 * Create a new game session for today's challenge
 * @returns {Promise<SessionResponse>} New session data
 */
export const createSession = () => {
  return api.post("/session");
};

/**
 * Get session state by session ID
 * @param {string} sessionId - Unique session identifier
 * @returns {Promise<SessionResponse>} Session state
 */
export const getSession = (sessionId) => {
  return api.get(`/session/${sessionId}`);
};

// ============================================================================
// CHALLENGE ENDPOINTS
// ============================================================================

/**
 * Get today's daily challenge
 * @returns {Promise<ChallengeResponse>} Today's challenge with 5 movies
 */
export const getTodayChallenge = () => {
  return api.get("/challenge/today");
};

/**
 * Get challenge for a specific date
 * @param {string} date - Date in ISO format (YYYY-MM-DD)
 * @returns {Promise<ChallengeResponse>} Challenge for the specified date
 */
export const getChallengeByDate = (date) => {
  return api.get(`/challenge/${date}`);
};

// ============================================================================
// CLUE ENDPOINTS
// ============================================================================

/**
 * Request a clue for the current movie
 * @param {string} sessionId - Unique session identifier
 * @param {string} clueType - Type of clue (YEAR, DIRECTOR, ACTOR_1, ACTOR_2, ACTOR_3, GENRE, PLOT_HINT, TITLE_HINT)
 * @returns {Promise<ClueResponse>} Clue value and updated session state
 */
export const requestClue = (sessionId, clueType) => {
  return api.post(`/clue/${clueType}`, { sessionId });
};

// ============================================================================
// GUESS ENDPOINTS
// ============================================================================

/**
 * Submit a movie guess
 * @param {string} sessionId - Unique session identifier
 * @param {number} movieId - TMDB movie ID being guessed
 * @returns {Promise<GuessResponse>} Guess result and updated score
 */
export const submitGuess = (sessionId, movieId) => {
  return api.post("/guess", { sessionId, movieId });
};

// ============================================================================
// ADMIN ENDPOINTS
// ============================================================================

/**
 * Force regeneration of today's challenge (admin only)
 * @returns {Promise<ChallengeResponse>} Newly generated challenge
 */
export const refreshChallenge = () => {
  return api.post("/admin/refresh");
};

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Configure API base URL (for testing or runtime changes)
 * @param {string} baseUrl - New base URL
 */
export const configureApi = (baseUrl) => {
  api.defaults.baseURL = baseUrl;
};

/**
 * Get the current API base URL
 * @returns {string} Current base URL
 */
export const getApiBaseUrl = () => {
  return api.defaults.baseURL;
};

// ============================================================================
// ERROR HANDLING HELPERS
// ============================================================================

/**
 * Extract error message from API error response
 * @param {Error} error - Axios or generic error
 * @returns {string} Error message
 */
export const getErrorMessage = (error) => {
  if (error.response) {
    // The request was made and the server responded with a status code
    // that falls out of the range of 2xx
    const data = error.response.data;
    if (data && data.message) {
      return data.message;
    }
    return `Server error: ${error.response.status}`;
  } else if (error.request) {
    // The request was made but no response was received
    return "No response from server. Please check your connection.";
  } else {
    // Something happened in setting up the request
    return error.message || "Request failed";
  }
};

export default api;
