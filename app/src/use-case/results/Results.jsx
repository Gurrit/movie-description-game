/**
 * Results Component
 * 
 * Displays game completion summary with score breakdown.
 */

import React, { useState, useEffect } from "react";
import { useParams, useHistory } from "react-router-dom";
import styled from "@emotion/styled";
import { Card, CardTitle } from "../../common/styling";
import AppButton from "../../common/app-button";
import Loading from "../../common/components/Loading";
import Error from "../../common/components/Error";
import { getSession, getTodayChallenge, createSession } from "../../api/client";
import MovieCard from "../game/MovieCard";

// Styled Components
const ResultsContainer = styled.div`
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 2rem;
`;

const ResultsHeader = styled(Card)`
  padding: 2rem;
  text-align: center;
`;

const ScoreSummary = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
`;

const ScoreTitle = styled.h2`
  font-family: "Roboto", sans-serif;
  color: white;
  font-size: 2rem;
  margin: 0;
`;

const ScoreValue = styled.div`
  font-family: "Roboto", sans-serif;
  font-size: 3rem;
  font-weight: bold;
  color: ${(props) => {
    const percentage = props.percentage || 0;
    if (percentage >= 80) return "#2ecc71";
    if (percentage >= 50) return "#f39c12";
    if (percentage >= 20) return "#e74c3c";
    return "#95a5a6";
  }};
`;

const ScoreLabel = styled.p`
  font-family: "Roboto", sans-serif;
  color: #95a5a6;
  font-size: 1rem;
  margin: 0;
`;

const ResultsTitle = styled(CardTitle)`
  font-size: 1.75rem;
  margin-bottom: 1.5rem;
`;

const MoviesGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
`;

const MovieResult = styled(Card)`
  padding: 1rem;
  position: relative;
`;

const CorrectBadge = styled.div`
  position: absolute;
  top: -10px;
  right: -10px;
  background: #2ecc71;
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: bold;
`;

const IncorrectBadge = styled(CorrectBadge)`
  background: #e74c3c;
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 1rem;
  justify-content: center;
`;

/**
 * Results Component
 * 
 * Shows game completion summary including:
 * - Final score
 * - List of all movies with correct/incorrect indicators
 * - Option to play again
 */
export default function Results() {
  const { sessionId } = useParams();
  const history = useHistory();
  const [session, setSession] = useState(null);
  const [challenge, setChallenge] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load session and challenge data
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);

      try {
        // Load session
        const { data: sessionData } = await getSession(sessionId);
        setSession(sessionData);

        // Load challenge
        const { data: challengeData } = await getTodayChallenge();
        setChallenge(challengeData);
      } catch (err) {
        setError(err.message || "Failed to load results. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [sessionId]);

  // Calculate statistics
  const totalPossible = 5000; // 5 movies * 1000 points
  const score = session?.currentScore || 0;
  const scorePercentage = Math.round((score / totalPossible) * 100);

  // Determine rating based on score
  const getRating = () => {
    if (scorePercentage >= 90) return { text: "Perfect!", emoji: "🎉" };
    if (scorePercentage >= 80) return { text: "Excellent!", emoji: "😊" };
    if (scorePercentage >= 70) return { text: "Great!", emoji: "👍" };
    if (scorePercentage >= 60) return { text: "Good", emoji: "😐" };
    if (scorePercentage >= 50) return { text: "Okay", emoji: "😕" };
    if (scorePercentage >= 30) return { text: "Need Practice", emoji: "😅" };
    return { text: "Keep Trying!", emoji: "😢" };
  };

  const handlePlayAgain = () => {
    history.push("/");
  };

  const handleNewGame = async () => {
    try {
      const { data } = await createSession();
      history.push(`/play/${data.sessionId}`);
    } catch (err) {
      setError(err.message || "Failed to start new game.");
    }
  };

  if (loading) {
    return <Loading message="Loading results..." fullPage />;
  }

  if (error) {
    return (
      <Error
        message={error}
        onRetry={() => window.location.reload()}
        onBack={() => history.push("/")}
        fullPage
      />
    );
  }

  if (!session || !challenge) {
    return <Loading message="Loading results..." fullPage />;
  }

  const rating = getRating();

  return (
    <ResultsContainer>
      <ResultsHeader>
        <ScoreSummary>
          <ScoreTitle>
            {rating.emoji} {rating.text}
          </ScoreTitle>
          <ScoreValue percentage={scorePercentage}>{score}</ScoreValue>
          <ScoreLabel>
            out of {totalPossible} points ({scorePercentage}%)
          </ScoreLabel>
        </ScoreSummary>
      </ResultsHeader>

      <Card>
        <ResultsTitle>Your Results</ResultsTitle>
        <MoviesGrid>
          {challenge.movies.map((movie) => {
            const wasCorrect = session.correctGuesses?.includes(movie.id);
            return (
              <MovieResult key={movie.id}>
                {wasCorrect ? (
                  <CorrectBadge>✓</CorrectBadge>
                ) : (
                  <IncorrectBadge>✗</IncorrectBadge>
                )}
                <MovieCard movie={movie} showDescription={false} />
              </MovieResult>
            );
          })}
        </MoviesGrid>

        <ButtonGroup style={{ marginTop: "2rem", paddingTop: "1rem", borderTop: "1px solid #333" }}>
          <AppButton onClick={handleNewGame} color="primary" variant="contained">
            Play New Game
          </AppButton>
          <AppButton onClick={handlePlayAgain} color="secondary" variant="outlined">
            Back to Home
          </AppButton>
        </ButtonGroup>
      </Card>
    </ResultsContainer>
  );
}
