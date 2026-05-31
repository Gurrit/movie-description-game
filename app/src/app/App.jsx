import React from "react";
import styled from "@emotion/styled";
import { Switch, Route } from "react-router-dom";
import Home from "../use-case/home";
import Game from "../use-case/game/Game";
import Results from "../use-case/results/Results";

const Header = styled.header`
  padding: 2rem;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
`;

const Heading = styled.h1`
  font-family: "Bebas Neue", cursive;
  color: #05c46b;
  font-size: 5rem;
  text-align: center;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
`;

const Layout = styled.div`
  min-height: 100vh;
  background: linear-gradient(to bottom, #16213e 0%, #0f3460 100%);
  color: #ecf0f1;
`;

const Main = styled.main`
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem 2rem;
`;

/**
 * Main App Component
 * 
 * Handles routing between Home, Game, and Results pages.
 * Manages global layout and header.
 */
const App = () => {
  return (
    <Layout>
      <Header>
        <Heading>Movie Description Game</Heading>
      </Header>
      <Main>
        <Switch>
          <Route exact path="/" component={Home} />
          <Route path="/play/:sessionId/results" component={Results} />
          <Route path="/play/:sessionId?" component={Game} />
        </Switch>
      </Main>
    </Layout>
  );
};

export default App;
