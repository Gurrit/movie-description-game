/**
 * MovieCard Component
 * 
 * Displays movie information after a correct guess.
 * Shows poster, title, year, director, genres, and description.
 */

import React from "react";
import { Card, CardTitle } from "../../common/styling";

/**
 * MovieCard Component
 * 
 * @param {Object} props
 * @param {Object} props.movie - Movie data to display
 * @param {boolean} props.showDescription - Whether to show description
 */
export default function MovieCard({ movie, showDescription = true }) {
  if (!movie) {
    return (
      <Card style={{ width: "100%", padding: "1rem" }}>
        <div style={{ textAlign: "center", padding: "2rem", color: "#7f8c8d", fontFamily: "'Roboto', sans-serif" }}>
          Guess the movie to reveal details!
        </div>
      </Card>
    );
  }

  // Format poster URL - ensure it has the base TMDB URL
  const posterUrl = movie.posterUrl
    ? (movie.posterUrl.startsWith("http") 
        ? movie.posterUrl 
        : `https://image.tmdb.org/t/p/w500${movie.posterUrl}`)
    : null;

  return (
    <Card style={{ width: "100%", padding: "1rem" }}>
      <CardTitle style={{ marginBottom: "1rem" }}>
        {movie.title}
        {movie.year && ` (${movie.year})`}
      </CardTitle>

      {posterUrl && (
        <img
          src={posterUrl}
          alt={movie.title}
          style={{ width: "100%", height: "auto", borderRadius: "0.5rem", marginBottom: "1rem", aspectRatio: "2/3", objectFit: "cover", background: "#222" }}
          onError={(e) => {
            e.target.style.display = "none";
          }}
        />
      )}

      <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
        {movie.directors?.length > 0 && (
          <p style={{ fontFamily: "'Roboto', sans-serif", fontSize: "0.875rem", color: "#bdc3c7", margin: "0.25rem 0", display: "flex", flexWrap: "wrap", gap: "0.5rem" }}>
            <span style={{ color: "#7f8c8d", fontWeight: "500" }}>Director:</span>
            <span style={{ color: "#ecf0f1" }}>{movie.directors.join(", ")}</span>
          </p>
        )}

        {movie.genres?.length > 0 && (
          <p style={{ fontFamily: "'Roboto', sans-serif", fontSize: "0.875rem", color: "#bdc3c7", margin: "0.25rem 0", display: "flex", flexWrap: "wrap", gap: "0.5rem" }}>
            <span style={{ color: "#7f8c8d", fontWeight: "500" }}>Genres:</span>
            <span style={{ color: "#ecf0f1" }}>{movie.genres.join(", ")}</span>
          </p>
        )}

        {movie.billings && Object.values(movie.billings).length > 0 && (
          <p style={{ fontFamily: "'Roboto', sans-serif", fontSize: "0.875rem", color: "#bdc3c7", margin: "0.25rem 0", display: "flex", flexWrap: "wrap", gap: "0.5rem" }}>
            <span style={{ color: "#7f8c8d", fontWeight: "500" }}>Actors:</span>
            <span style={{ color: "#ecf0f1" }}>{Object.values(movie.billings).join(", ")}</span>
          </p>
        )}

        {showDescription && movie.description && (
          <p style={{ fontFamily: "'Roboto', sans-serif", fontSize: "0.875rem", color: "#95a5a6", lineHeight: "1.5", marginTop: "0.5rem" }}>
            {movie.description}
          </p>
        )}
      </div>
    </Card>
  );
}
