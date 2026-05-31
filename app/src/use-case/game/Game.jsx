/**
 * Game Container Component
 * 
 * Main game page that orchestrates the game UI.
 * Manages game flow between clue requests and guess submissions.
 */

import React from "react";
import { useParams, useHistory } from "react-router-dom";
import styled from "@emotion/styled";
import CluePanel from "./CluePanel";
import GuessForm from "./GuessForm";
import MovieCard from "./MovieCard";
import ScoreDisplay from "./ScoreDisplay";
import ProgressIndicator from "./ProgressIndicator";
import { useGame } from "../../common/hooks/useGame";
import Loading from "../../common/components/Loading";
import Error from "../../common/components/Error";
import AppButton from "../../common/app-button";

// Styled Components
const GameContainer = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
`;

const GameHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

const MovieSection = styled.div`
  display: flex;
  gap: 2rem;
  align-items: flex-start;
`;

const MainContent = styled.div`
  flex: 1;
`;

const Sidebar = styled.div`
  width: 300px;
  min-width: 300px;
`;

const GameComplete = styled.div`
  text-align: center;
  padding: 3rem;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 1rem;
`;

const GameTitle = styled.h2`
  font-family: "Roboto", sans-serif;
  color: white;
  font-size: 2rem;
`;

/**
 * Main Game Component
 * Handles game flow, state management, and renders appropriate UI
 */
export default function Game() {
  const { sessionId } = useParams();
  const history = useHistory();

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
  }, [hasWon, sessionId, history]);

  const handleNewGame = () => {
    resetGame();
    history.push("/");
  };

  const handleGameWon = () => {
    // Navigate to results or show modal
    history.push(`/play/${sessionId}/results`);
  };

  if (loading && !session) {
    return <Loading message="Loading game..." />;
  }

  if (error) {
    return (
      <Error
        message={error}
        onRetry={initGame}
        onBack={() => history.push("/")}
      />
    );
  }

  if (!session || !challenge) {
    return <Loading message="Setting up game..." />;
  }

  return (
    <GameContainer>
      <GameHeader>
        <GameTitle>
          Movie {currentMovieIndex + 1} of {totalMovies}
        </GameTitle>
        <ScoreDisplay
          score={currentScore}
          total={totalMovies * 1000}
          percentage={scorePercentage}
        />
      </GameHeader>

      <ProgressIndicator
        current={currentMovieIndex + 1}
        total={totalMovies}
        correctGuesses={session.correctGuesses || []}
      />

      <MovieSection>
        <MainContent>
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
        </MainContent>

        <Sidebar>
          {hasWon ? (
            <GameComplete>
              <h3>Congratulations! You won!</h3>
              <p>Final Score: {currentScore}</p>
              <AppButton onClick={handleGameWon}>
                View Results
              </AppButton>
              <AppButton onClick={handleNewGame} variant="secondary">
                Play Again
              </AppButton>
            </GameComplete>
          ) : (
            <>
              <MovieCard movie={currentMovie} />
              {currentMovieIndex > 0 && (
                <AppButton onClick={() => history.goBack()}>
                  Back to Previous
                </AppButton>
              )}
            </>
          )}
        </Sidebar>
      </MovieSection>
    </GameContainer>
  );
}
