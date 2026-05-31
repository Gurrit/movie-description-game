/**
 * useGame Hook
 * 
 * Manages game session state, clue requests, and guess submissions.
 * Handles loading, errors, and automatic session management.
 */

import { useState, useEffect, useCallback } from "react";
import {
  createSession,
  getSession,
  getTodayChallenge,
  requestClue,
  submitGuess,
  getErrorMessage,
} from "../../api/client";

// Clue type constants for validation
const VALID_CLUE_TYPES = [
  "YEAR",
  "DIRECTOR",
  "ACTOR_1",
  "ACTOR_2",
  "ACTOR_3",
  "GENRE",
  "PLOT_HINT",
  "TITLE_HINT",
];

/**
 * Custom hook for managing game state
 * @param {string} sessionId - Optional session ID to resume
 * @returns {Object} Game state and actions
 */
export function useGame(sessionId) {
  // State
  const [session, setSession] = useState(null);
  const [challenge, setChallenge] = useState(null);
  const [currentMovie, setCurrentMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [clues, setClues] = useState([]); // Array of {type, value} for current movie
  const [guesses, setGuesses] = useState([]); // Array of guess results

  // Derived state
  const currentMovieIndex = session?.currentMovieIndex || 0;
  const currentScore = session?.currentScore || 0;
  const totalMovies = challenge?.movies?.length || 0;
  const remainingMovies = session?.remainingMovies || [];
  const hasWon = session?.correctGuesses?.length >= totalMovies;

  // Initialize or load session and challenge
  const initGame = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      // Get or create session
      let sessionData;
      if (sessionId) {
        // Resume existing session
        const { data } = await getSession(sessionId);
        sessionData = data;
      } else {
        // Create new session
        const { data } = await createSession();
        sessionData = data;
      }
      setSession(sessionData);

      // Load today's challenge
      const { data: challengeData } = await getTodayChallenge();
      setChallenge(challengeData);

      // Set current movie based on session index
      const movieIndex = sessionData.currentMovieIndex || 0;
      if (challengeData.movies && challengeData.movies[movieIndex]) {
        setCurrentMovie(challengeData.movies[movieIndex]);
      }

      // Initialize clues from session
      const currentClues = sessionData.cluesUsed || [];
      // Filter clues for current movie only
      // Note: session.cluesUsed might be structured differently based on backend
      setClues(currentClues);

      // Initialize guesses
      setGuesses(sessionData.guessedMovies || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  // Request a clue for the current movie
  const getClue = useCallback(
    async (clueType) => {
      // Validate clue type
      if (!VALID_CLUE_TYPES.includes(clueType)) {
        setError(`Invalid clue type: ${clueType}`);
        return null;
      }

      if (!session?.sessionId) {
        setError("No active session. Please start a game first.");
        return null;
      }

      try {
        setLoading(true);
        setError(null);

        const { data } = await requestClue(session.sessionId, clueType);

        // Update session state
        setSession(data);

        // Add clue to local state
        setClues((prevClues) => [
          ...prevClues,
          { type: clueType, value: data.clue },
        ]);

        return data.clue;
      } catch (err) {
        setError(getErrorMessage(err));
        return null;
      } finally {
        setLoading(false);
      }
    },
    [session]
  );

  // Submit a guess for the current movie
  const guessMovie = useCallback(
    async (movieId) => {
      if (!session?.sessionId) {
        setError("No active session. Please start a game first.");
        return null;
      }

      try {
        setLoading(true);
        setError(null);

        const { data } = await submitGuess(session.sessionId, movieId);

        // Update session state
        setSession(data);

        // Add guess to history
        setGuesses((prevGuesses) => [
          ...prevGuesses,
          {
            movieId,
            correct: data.correct,
            score: data.score,
            timestamp: new Date().toISOString(),
          },
        ]);

        // If correct, update current movie to next one
        if (data.correct && data.remainingMovies?.length > 0) {
          // The backend should advance currentMovieIndex
          // But we also update locally for immediate UI feedback
          const nextIndex = data.currentMovieIndex || currentMovieIndex + 1;
          if (challenge?.movies?.[nextIndex]) {
            setCurrentMovie(challenge.movies[nextIndex]);
            setClues([]); // Reset clues for new movie
          }
        }

        // Clear clues if movie was guessed (correct or incorrect)
        // User moves to next movie on correct guess
        // On incorrect guess, they can keep trying with more clues

        return data;
      } catch (err) {
        setError(getErrorMessage(err));
        return null;
      } finally {
        setLoading(false);
      }
    },
    [session, challenge, currentMovieIndex]
  );

  // Move to next movie manually (if needed)
  const nextMovie = useCallback(() => {
    if (!challenge?.movies || !session) return;

    const nextIndex = Math.min(currentMovieIndex + 1, challenge.movies.length - 1);
    
    // Check if we should advance (user might want to skip)
    if (nextIndex <= currentMovieIndex) return;

    // Update local state
    setCurrentMovie(challenge.movies[nextIndex]);
    setClues([]); // Reset clues for new movie

    // Update session
    setSession((prev) => ({
      ...prev,
      currentMovieIndex: nextIndex,
    }));
  }, [challenge, currentMovieIndex, session]);

  // Reset game state
  const resetGame = useCallback(() => {
    setSession(null);
    setChallenge(null);
    setCurrentMovie(null);
    setClues([]);
    setGuesses([]);
    setError(null);
    setLoading(false);
  }, []);

  // Auto-initialize on mount
  useEffect(() => {
    initGame();
  }, [initGame]);

  // Update current movie when session or challenge changes
  useEffect(() => {
    if (session && challenge?.movies) {
      const movieIndex = session.currentMovieIndex || 0;
      if (challenge.movies[movieIndex]) {
        setCurrentMovie(challenge.movies[movieIndex]);
      }
    }
  }, [session, challenge]);

  return {
    // State
    session,
    challenge,
    currentMovie,
    currentMovieIndex,
    currentScore,
    totalMovies,
    remainingMovies,
    clues,
    guesses,
    hasWon,
    loading,
    error,

    // Actions
    getClue,
    guessMovie,
    nextMovie,
    initGame,
    resetGame,

    // Computed
    totalPossibleScore: totalMovies * 1000,
    scorePercentage: totalMovies > 0
      ? Math.round((currentScore / (totalMovies * 1000)) * 100)
      : 0,
  };
}

export default useGame;
