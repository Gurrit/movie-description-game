import React, { useState } from "react";
import { useHistory } from "react-router-dom";
import styled from "@emotion/styled";
import { Card, CardTitle } from "../../common/styling";
import AppButton from "../../common/app-button";
import Loading from "../../common/components/Loading";
import { createSession } from "../../api/client";

const HomeContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
  padding: 2rem;
`;

const Info = styled(Card)`
  display: grid;
  grid-auto-rows: min-content;
  grid-template-columns: 1fr;
  grid-gap: 2rem;
  justify-items: center;
  max-width: 500px;
  width: 100%;
`;

const HomeTitle = styled(CardTitle)`
  font-size: 2.5rem;
  text-align: center;
`;

const HomeDescription = styled.p`
  font-family: "Roboto", sans-serif;
  color: #bdc3c7;
  font-size: 1rem;
  text-align: center;
  line-height: 1.6;
  max-width: 400px;
`;

const ButtonContainer = styled.div`
  display: flex;
  gap: 1rem;
  width: 100%;
  justify-content: center;
`;

/**
 * Home Component
 * 
 * Landing page with option to start a new game.
 * In the future, could show past games or leaderboard.
 */
const Home = () => {
  const history = useHistory();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleStartGame = async () => {
    setLoading(true);
    setError(null);

    try {
      const { data } = await createSession();
      history.push(`/play/${data.sessionId}`);
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
    <HomeContainer>
      <Info>
        <HomeTitle>Movie Description Game</HomeTitle>
        <HomeDescription>
          Guess 5 movies each day based on clues like year, director, actors, and genre.
          Earn points for each correct guess, but be careful - each clue costs points!
        </HomeDescription>
        
        <ButtonContainer>
          <AppButton
            onClick={handleStartGame}
            size="large"
            fullWidth
          >
            Start New Game
          </AppButton>
        </ButtonContainer>

        {error && (
          <p style={{ color: "#e74c3c", textAlign: "center" }}>{error}</p>
        )}
      </Info>
    </HomeContainer>
  );
};

export default Home;
