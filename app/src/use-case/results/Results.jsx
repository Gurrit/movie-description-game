/**
 * Results Component
 * 
 * Displays game completion summary with score breakdown.
 */

import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardTitle } from "../../common/styling";
import AppButton from "../../common/app-button";
import Loading from "../../common/components/Loading";
import Error from "../../common/components/Error";
import { getSession, getTodayChallenge, createSession } from "../../api/client";
import MovieCard from "../game/MovieCard";

// Helper function for score color
const getScoreColor = (percentage) => {
  if (percentage >= 80) return "#2ecc71";
  if (percentage >= 50) return "#f39c12";
  if (percentage >= 20) return "#e74c3c";
  return "#95a5a6";
};

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
  const navigate = useNavigate();
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
    navigate("/");
  };

  const handleNewGame = async () => {
    try {
      const { data } = await createSession();
      navigate(`/play/${data.sessionId}`);
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
        onBack={() => navigate("/")}
        fullPage
      />
    );
  }

  if (!session || !challenge) {
    return <Loading message="Loading results..." fullPage />;
  }

  const rating = getRating();

  return (
    <div className="mx-auto p-4" style={{ maxWidth: "800px", display: "flex", flexDirection: "column", gap: "2rem" }}>
      <Card className="p-4 text-center">
        <div className="d-flex flex-column gap-3 align-items-center">
          <h2 style={{ fontFamily: "'Roboto', sans-serif", color: "white", fontSize: "2rem", margin: "0" }}>
            {rating.emoji} {rating.text}
          </h2>
          <div style={{ fontFamily: "'Roboto', sans-serif", fontSize: "3rem", fontWeight: "bold", color: getScoreColor(scorePercentage) }}>
            {score}
          </div>
          <p style={{ fontFamily: "'Roboto', sans-serif", color: "#95a5a6", fontSize: "1rem", margin: "0" }}>
            out of {totalPossible} points ({scorePercentage}%)
          </p>
        </div>
      </Card>

      <Card>
        <CardTitle className="text-center" style={{ fontSize: "1.75rem", marginBottom: "1.5rem" }}>
          Your Results
        </CardTitle>
        <div className="d-grid gap-3" style={{ gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))" }}>
          {challenge.movies.map((movie) => {
            const wasCorrect = session.correctGuesses?.includes(movie.id);
            return (
              <div key={movie.id} style={{ position: "relative" }}>
                <div 
                  style={{
                    position: "absolute",
                    top: "-10px",
                    right: "-10px",
                    background: wasCorrect ? "#2ecc71" : "#e74c3c",
                    color: "white",
                    width: "30px",
                    height: "30px",
                    borderRadius: "50%",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: "0.875rem",
                    fontWeight: "bold",
                  }}
                >
                  {wasCorrect ? "✓" : "✗"}
                </div>
                <MovieCard movie={movie} showDescription={false} />
              </div>
            );
          })}
        </div>

        <div className="d-flex gap-3 justify-content-center mt-4 pt-3" style={{ borderTop: "1px solid #333" }}>
          <AppButton onClick={handleNewGame} variant="primary">
            Play New Game
          </AppButton>
          <AppButton onClick={handlePlayAgain} variant="outline-secondary">
            Back to Home
          </AppButton>
        </div>
      </Card>
    </div>
  );
}
