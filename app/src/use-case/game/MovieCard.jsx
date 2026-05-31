/**
 * MovieCard Component
 * 
 * Displays movie information after a correct guess.
 * Shows poster, title, year, director, genres, and description.
 */

import React from "react";
import styled from "@emotion/styled";
import { Card, CardTitle } from "../../common/styling";

// Styled Components
const MovieCardContainer = styled(Card)`
  width: 100%;
  padding: 1rem;
`;

const MoviePoster = styled.img`
  width: 100%;
  height: auto;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
  aspect-ratio: 2/3;
  object-fit: cover;
  background: #222;
`;

const MovieDetails = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const MovieMeta = styled.p`
  font-family: "Roboto", sans-serif;
  font-size: 0.875rem;
  color: #bdc3c7;
  margin: 0.25rem 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
`;

const MetaLabel = styled.span`
  color: #7f8c8d;
  font-weight: 500;
`;

const MetaValue = styled.span`
  color: #ecf0f1;
`;

const MovieDescription = styled.p`
  font-family: "Roboto", sans-serif;
  font-size: 0.875rem;
  color: #95a5a6;
  line-height: 1.5;
  margin-top: 0.5rem;
`;

const NoMovie = styled.div`
  text-align: center;
  padding: 2rem;
  color: #7f8c8d;
  font-family: "Roboto", sans-serif;
`;

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
      <MovieCardContainer>
        <NoMovie>Guess the movie to reveal details!</NoMovie>
      </MovieCardContainer>
    );
  }

  // Format poster URL - ensure it has the base TMDB URL
  const posterUrl = movie.posterUrl
    ? (movie.posterUrl.startsWith("http") 
        ? movie.posterUrl 
        : `https://image.tmdb.org/t/p/w500${movie.posterUrl}`)
    : null;

  return (
    <MovieCardContainer>
      <CardTitle style={{ marginBottom: "1rem" }}>
        {movie.title}
        {movie.year && ` (${movie.year})`}
      </CardTitle>

      {posterUrl && (
        <MoviePoster
          src={posterUrl}
          alt={movie.title}
          onError={(e) => {
            e.target.style.display = "none";
          }}
        />
      )}

      <MovieDetails>
        {movie.directors?.length > 0 && (
          <MovieMeta>
            <MetaLabel>Director:</MetaLabel>
            <MetaValue>{movie.directors.join(", ")}</MetaValue>
          </MovieMeta>
        )}

        {movie.genres?.length > 0 && (
          <MovieMeta>
            <MetaLabel>Genres:</MetaLabel>
            <MetaValue>{movie.genres.join(", ")}</MetaValue>
          </MovieMeta>
        )}

        {movie.billings && Object.values(movie.billings).length > 0 && (
          <MovieMeta>
            <MetaLabel>Actors:</MetaLabel>
            <MetaValue>{Object.values(movie.billings).join(", ")}</MetaValue>
          </MovieMeta>
        )}

        {showDescription && movie.description && (
          <MovieDescription>{movie.description}</MovieDescription>
        )}
      </MovieDetails>
    </MovieCardContainer>
  );
}
