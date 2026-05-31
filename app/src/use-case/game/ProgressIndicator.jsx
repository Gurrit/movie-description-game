/**
 * ProgressIndicator Component
 * 
 * Shows progress through the 5 movies.
 * Visual indicator of which movie the user is on.
 */

import React from "react";

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

  const getStyle = (isActive, isCompleted) => ({
    width: "32px",
    height: "32px",
    borderRadius: "50%",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontFamily: "'Roboto', sans-serif",
    fontSize: "0.875rem",
    fontWeight: "bold",
    color: "white",
    cursor: "default",
    border: `2px solid ${isActive ? "#3498db" : isCompleted ? "#2ecc71" : "#555"}`,
    background: isActive ? "#3498db" : isCompleted ? "#2ecc71" : "transparent",
    transition: "all 0.2s ease",
  });

  return (
    <div className="d-flex align-items-center gap-3 p-2">
      <span style={{ fontFamily: "'Roboto', sans-serif", color: "#7f8c8d", fontSize: "0.875rem" }}>
        Progress:
      </span>
      <div className="d-flex gap-2">
        {movieStates.map((movie) => (
          <div
            key={movie.index}
            style={getStyle(movie.isActive, movie.isCompleted)}
            onMouseEnter={(e) => e.target.style.transform = "scale(1.1)"}
            onMouseLeave={(e) => e.target.style.transform = "scale(1)"}
          >
            {movie.isCompleted ? "✓" : movie.index + 1}
          </div>
        ))}
      </div>
    </div>
  );
}
