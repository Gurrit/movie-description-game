/**
 * Game Container Component
 * 
 * Main game page that orchestrates the game UI.
 * Manages game flow between clue requests and guess submissions.
 */

import React from "react";
import { useParams, useNavigate } from "react-router-dom";
import CluePanel from "./CluePanel";
import GuessForm from "./GuessForm";
import MovieCard from "./MovieCard";
import ScoreDisplay from "./ScoreDisplay";
import ProgressIndicator from "./ProgressIndicator";
import { useGame } from "../../common/hooks/useGame";
import Loading from "../../common/components/Loading";
import Error from "../../common/components/Error";
import AppButton from "../../common/app-button";

/**
 * Main Game Component
 * Handles game flow, state management, and renders appropriate UI
 */
export default function Game() {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const {
    session,
    challenge,
    currentMovie,
    currentMovieIndex,
    currentScore,
    totalMovies,
    hasWon,
    loading,
    error,
    getClue,
    guessMovie,
    initGame,
    resetGame,
    scorePercentage,
  } = useGame(sessionId);

  // Handle game completion
  React.useEffect(() => {
    if (hasWon && sessionId) {
      // Could auto-redirect to results page, or show overlay
    }
  }, [hasWon, sessionId, navigate]);

  const handleNewGame = () => {
    resetGame();
    navigate("/");
  };

  const handleGameWon = () => {
    // Navigate to results or show modal
    navigate(`/play/${sessionId}/results`);
  };

  if (loading && !session) {
    return <Loading message="Loading game..." />;
  }

  if (error) {
    return (
      <Error
        message={error}
        onRetry={initGame}
        onBack={() => navigate("/")}
      />
    );
  }

  if (!session || !challenge) {
    return <Loading message="Setting up game..." />;
  }

  return (
    <div className="mx-auto p-4" style={{ maxWidth: "800px", display: "flex", flexDirection: "column", gap: "2rem" }}>
      <div className="d-flex justify-content-between align-items-center">
        <h2 style={{ fontFamily: "'Roboto', sans-serif", color: "white", fontSize: "2rem" }}>
          Movie {currentMovieIndex + 1} of {totalMovies}
        </h2>
        <ScoreDisplay
          score={currentScore}
          total={totalMovies * 1000}
          percentage={scorePercentage}
        />
      </div>

      <ProgressIndicator
        current={currentMovieIndex + 1}
        total={totalMovies}
        correctGuesses={session.correctGuesses || []}
      />

      <div className="d-flex gap-4 align-items-start">
        <div className="flex-grow-1">
          <CluePanel
            sessionId={session.sessionId}
            usedClues={session.cluesUsed || []}
            onRequestClue={getClue}
          />

          <GuessForm
            sessionId={session.sessionId}
            movies={challenge.movies}
            onGuess={guessMovie}
            disabled={hasWon}
          />
        </div>

        <div style={{ width: "300px", minWidth: "300px" }}>
          {hasWon ? (
            <div className="text-center p-4" style={{ background: "rgba(0, 0, 0, 0.3)", borderRadius: "1rem" }}>
              <h3>Congratulations! You won!</h3>
              <p>Final Score: {currentScore}</p>
              <AppButton onClick={handleGameWon}>
                View Results
              </AppButton>
              <AppButton onClick={handleNewGame} variant="secondary" className="ms-2">
                Play Again
              </AppButton>
            </div>
          ) : (
            <>
              <MovieCard movie={currentMovie} />
              {currentMovieIndex > 0 && (
                <AppButton onClick={() => navigate(-1)} className="mt-2">
                  Back to Previous
                </AppButton>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
