/**
 * REST API Client for Movie Description Game
 *
 * The endpoint functions are thin wrappers around the client generated from
 * backend/api/openapi.yaml (see ./generated, regenerate with `yarn generate:api`).
 * Do not hand-write request paths or payload shapes here; change the spec instead.
 *
 * Base URL is configured via REACT_APP_API_BASE environment variable.
 * Defaults to http://localhost:8080/api for development.
 */

import axios from "axios";
import {
  AdminApi,
  ChallengeApi,
  ClueApi,
  Configuration,
  GuessApi,
  SessionApi,
} from "./generated";

// Configuration
const API_BASE = process.env.REACT_APP_API_BASE || "http://localhost:8080/api";

/**
 * Axios instance with base URL and default headers.
 * Its baseURL takes precedence over the generated clients' default base path.
 */
const api = axios.create({
  baseURL: API_BASE,
  headers: {
    "Content-Type": "application/json",
  },
});

const configuration = new Configuration({ basePath: API_BASE });
const adminApi = new AdminApi(configuration, undefined, api);
const challengeApi = new ChallengeApi(configuration, undefined, api);
const clueApi = new ClueApi(configuration, undefined, api);
const guessApi = new GuessApi(configuration, undefined, api);
const sessionApi = new SessionApi(configuration, undefined, api);

// ============================================================================
// SESSION ENDPOINTS
// ============================================================================

/**
 * Create a new game session for today's challenge
 * @returns {Promise<import("axios").AxiosResponse<SessionResponse>>} New session data
 */
export const createSession = () => {
  return sessionApi.createSession();
};

/**
 * Get session state by session ID
 * @param {string} sessionId - Unique session identifier
 * @returns {Promise<import("axios").AxiosResponse<SessionResponse>>} Session state
 */
export const getSession = (sessionId) => {
  return sessionApi.getSession({ sessionId });
};

// ============================================================================
// CHALLENGE ENDPOINTS
// ============================================================================

/**
 * Get today's daily challenge
 * @returns {Promise<import("axios").AxiosResponse<ChallengeResponse>>} Today's challenge with 5 movies
 */
export const getTodayChallenge = () => {
  return challengeApi.getTodayChallenge();
};

/**
 * Get challenge for a specific date
 * @param {string} date - Date in ISO format (YYYY-MM-DD)
 * @returns {Promise<import("axios").AxiosResponse<ChallengeResponse>>} Challenge for the specified date
 */
export const getChallengeByDate = (date) => {
  return challengeApi.getChallengeByDate({ date });
};

// ============================================================================
// CLUE ENDPOINTS
// ============================================================================

/**
 * Request a clue for the current movie
 * @param {string} sessionId - Unique session identifier
 * @param {string} clueType - Type of clue (YEAR, DIRECTOR, ACTOR_1, ACTOR_2, ACTOR_3, GENRE, PLOT_HINT, TITLE_HINT)
 * @returns {Promise<import("axios").AxiosResponse<ClueResponse>>} Clue value and updated session state
 */
export const requestClue = (sessionId, clueType) => {
  return clueApi.getClue({ type: clueType, clueRequest: { sessionId } });
};

// ============================================================================
// GUESS ENDPOINTS
// ============================================================================

/**
 * Submit a movie guess
 * @param {string} sessionId - Unique session identifier
 * @param {number} movieId - TMDB movie ID being guessed
 * @returns {Promise<import("axios").AxiosResponse<GuessResponse>>} Guess result and updated score
 */
export const submitGuess = (sessionId, movieId) => {
  return guessApi.submitGuess({ guessRequest: { sessionId, movieId } });
};

// ============================================================================
// ADMIN ENDPOINTS
// ============================================================================

/**
 * Force regeneration of today's challenge (admin only)
 * @returns {Promise<import("axios").AxiosResponse<ChallengeResponse>>} Newly generated challenge
 */
export const refreshChallenge = () => {
  return adminApi.refreshChallenge();
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
