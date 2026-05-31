/**
 * GuessForm Component
 * 
 * Form for submitting movie guesses with autocomplete.
 * Handles selection and submission of guesses.
 */

import React, { useState, useEffect } from "react";
import AppButton from "../../common/app-button";
import MovieAutocomplete from "../guess/movie-autocomplete/MovieAutocomplete.comp";

/**
 * GuessForm Component
 * 
 * @param {Object} props
 * @param {string} props.sessionId - Current session ID
 * @param {Array<Object>} props.movies - List of movies to guess from
 * @param {Function} props.onGuess - Callback when guess is submitted
 * @param {boolean} props.disabled - Whether form is disabled
 */
export default function GuessForm({ sessionId, movies = [], onGuess, disabled = false }) {
  const [selectedMovie, setSelectedMovie] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Clear feedback when disabled changes
  useEffect(() => {
    if (disabled) {
      setSelectedMovie(null);
      setFeedback(null);
    }
  }, [disabled]);

  // Handle movie selection from autocomplete
  const handleSelectMovie = (movie) => {
    setSelectedMovie(movie);
    setFeedback(null);
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    if (e) e.preventDefault();

    if (!selectedMovie || disabled || submitting) return;

    setSubmitting(true);
    setFeedback(null);

    try {
      const result = await onGuess(selectedMovie.id);

      if (result) {
        if (result.correct) {
          setFeedback({
            message: `Correct! ${selectedMovie.title} (${selectedMovie.year}) - +${result.score} points`,
            success: true,
          });
        } else {
          setFeedback({
            message: result.message || "Incorrect guess. Try again!",
            error: true,
          });
        }
      }

      // Clear selection after guess
      setSelectedMovie(null);
    } catch (err) {
      setFeedback({
        message: err.message || "Failed to submit guess. Please try again.",
        error: true,
      });
    } finally {
      setSubmitting(false);
    }
  };

  // Handle keyboard submission
  const handleKeyDown = (e) => {
    if (e.key === "Enter" && selectedMovie && !submitting) {
      handleSubmit(e);
    }
  };

  return (
    <div className="p-4 mb-3" style={{ background: "rgba(0, 0, 0, 0.2)", borderRadius: "0.75rem" }} onSubmit={handleSubmit}>
      <h3 style={{ fontFamily: "'Roboto', sans-serif", color: "white", marginBottom: "1rem", fontSize: "1.25rem" }}>
        What is this movie?
      </h3>

      {feedback && (
        <div 
          className="p-3 mb-3 rounded text-center"
          style={{
            fontFamily: "'Roboto', sans-serif",
            fontSize: "0.9rem",
            background: feedback.success ? "rgba(46, 204, 113, 0.2)" : feedback.error ? "rgba(231, 76, 60, 0.2)" : "rgba(149, 165, 166, 0.2)",
            color: feedback.success ? "#2ecc71" : feedback.error ? "#e74c3c" : "#95a5a6",
            border: feedback.success ? "1px solid #2ecc71" : feedback.error ? "1px solid #e74c3c" : "none"
          }}
        >
          {feedback.message}
        </div>
      )}

      <div className="mb-3" onKeyDown={handleKeyDown}>
        <MovieAutocomplete
          movies={movies}
          selectedMovie={selectedMovie}
          onSelect={handleSelectMovie}
          disabled={disabled || submitting}
          placeholder="Start typing to search movies..."
        />
      </div>

      <AppButton
        type="submit"
        onClick={handleSubmit}
        disabled={!selectedMovie || disabled || submitting}
        fullWidth
        size="large"
      >
        {submitting ? "Submitting..." : "Submit Guess"}
      </AppButton>
    </div>
  );
}
