import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, CardTitle } from "../../common/styling";
import AppButton from "../../common/app-button";
import Loading from "../../common/components/Loading";
import { createSession } from "../../api/client";

/**
 * Home Component
 * 
 * Landing page with option to start a new game.
 * In the future, could show past games or leaderboard.
 */
const Home = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleStartGame = async () => {
    setLoading(true);
    setError(null);

    try {
      const { data } = await createSession();
      navigate(`/play/${data.sessionId}`);
    } catch (err) {
      setError(err.message || "Failed to start game. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <Loading message="Creating your game..." fullPage />;
  }

  return (
    <div style={{ minHeight: "calc(100vh - 200px)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "2rem" }}>
      <Card className="d-flex flex-column align-items-center justify-content-center gap-3" style={{ maxWidth: "500px", width: "100%", padding: "2rem" }}>
        <CardTitle className="text-center" style={{ fontSize: "2.5rem" }}>
          Movie Description Game
        </CardTitle>
        <p style={{ fontFamily: "'Roboto', sans-serif", color: "#bdc3c7", fontSize: "1rem", textAlign: "center", lineHeight: "1.6", maxWidth: "400px" }}>
          Guess 5 movies each day based on clues like year, director, actors, and genre.
          Earn points for each correct guess, but be careful - each clue costs points!
        </p>
        
        <div className="d-flex gap-3 w-100 justify-content-center">
          <AppButton
            onClick={handleStartGame}
            size="large"
            fullWidth
          >
            Start New Game
          </AppButton>
        </div>

        {error && (
          <p style={{ color: "#e74c3c", textAlign: "center" }}>{error}</p>
        )}
      </Card>
    </div>
  );
};

export default Home;
