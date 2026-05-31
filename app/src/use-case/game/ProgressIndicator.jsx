/**
 * ProgressIndicator Component
 * 
 * Shows progress through the 5 movies.
 * Visual indicator of which movie the user is on.
 */

import React from "react";
import styled from "@emotion/styled";

// Styled Components
const ProgressContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem;
`;

const ProgressLabel = styled.span`
  font-family: "Roboto", sans-serif;
  color: #7f8c8d;
  font-size: 0.875rem;
`;

const MovieDotsContainer = styled.div`
  display: flex;
  gap: 0.5rem;
`;

const MovieDot = styled.div`
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "Roboto", sans-serif;
  font-size: 0.875rem;
  font-weight: bold;
  color: white;
  cursor: default;
  border: 2px solid ${(props) => (props.active ? "#3498db" : props.completed ? "#2ecc71" : "#555")};
  background: ${(props) => {
    if (props.active) return "#3498db";
    if (props.completed) return "#2ecc71";
    return "transparent";
  }};
  transition: all 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }
`;

/**
 * ProgressIndicator Component
 * 
 * @param {Object} props
 * @param {number} props.current - Current movie index (1-based)
 * @param {number} props.total - Total movies
 * @param {Array<number>} props.correctGuesses - List of correctly guessed movie IDs
 */
export default function ProgressIndicator({ current = 1, total = 5, correctGuesses = [] }) {
  // Build array of movie states
  const movieStates = Array.from({ length: total }, (_, index) => {
    const isActive = index + 1 === current;
    const isCompleted = correctGuesses.length > index;
    return { index, isActive, isCompleted };
  });

  return (
    <ProgressContainer>
      <ProgressLabel>Progress:</ProgressLabel>
      <MovieDotsContainer>
        {movieStates.map((movie) => (
          <MovieDot
            key={movie.index}
            active={movie.isActive}
            completed={movie.isCompleted}
          >
            {movie.isCompleted ? "✓" : movie.index + 1}
          </MovieDot>
        ))}
      </MovieDotsContainer>
    </ProgressContainer>
  );
}
