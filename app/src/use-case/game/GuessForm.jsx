/**
 * GuessForm Component
 * 
 * Form for submitting movie guesses with autocomplete.
 * Handles selection and submission of guesses.
 */

import React, { useState, useEffect } from "react";
import styled from "@emotion/styled";
import AppButton from "../../common/app-button";
import MovieAutocomplete from "../guess/movie-autocomplete/MovieAutocomplete.comp";

// Styled Components
const GuessFormContainer = styled.div`
  background: rgba(0, 0, 0, 0.2);
  padding: 1.5rem;
  border-radius: 0.75rem;
`;

const GuessFormTitle = styled.h3`
  font-family: "Roboto", sans-serif;
  color: white;
  margin-bottom: 1rem;
  font-size: 1.25rem;
`;

const SubmitButton = styled(AppButton)`
  width: 100%;
  margin-top: 1rem;
  padding: 1rem 2rem;
  font-size: 1rem;
`;

const GuessInputContainer = styled.div`
  margin-bottom: 1rem;
`;

const FeedbackMessage = styled.div`
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  margin: 1rem 0;
  font-family: "Roboto", sans-serif;
  font-size: 0.9rem;
  text-align: center;

  ${(props) => {
    if (props.success) {
      return {
        background: "rgba(46, 204, 113, 0.2)",
        color: "#2ecc71",
        border: "1px solid #2ecc71",
      };
    }
    if (props.error) {
      return {
        background: "rgba(231, 76, 60, 0.2)",
        color: "#e74c3c",
        border: "1px solid #e74c3c",
      };
    }
    return {
      background: "rgba(149, 165, 166, 0.2)",
      color: "#95a5a6",
    };
  }}
`;

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
    <GuessFormContainer onSubmit={handleSubmit}>
      <GuessFormTitle>What is this movie?</GuessFormTitle>

      {feedback && (
        <FeedbackMessage
          success={feedback.success}
          error={feedback.error}
        >
          {feedback.message}
        </FeedbackMessage>
      )}

      <GuessInputContainer onKeyDown={handleKeyDown}>
        <MovieAutocomplete
          movies={movies}
          selectedMovie={selectedMovie}
          onSelect={handleSelectMovie}
          disabled={disabled || submitting}
          placeholder="Start typing to search movies..."
        />
      </GuessInputContainer>

      <SubmitButton
        type="submit"
        onClick={handleSubmit}
        disabled={!selectedMovie || disabled || submitting}
      >
        {submitting ? "Submitting..." : "Submit Guess"}
      </SubmitButton>
    </GuessFormContainer>
  );
}
