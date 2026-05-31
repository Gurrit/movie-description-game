// This file is deprecated. The new game UI is in use-case/game/Game.jsx
// Kept for reference but not used in the new REST API-based frontend.

import React from "react";
import styled from "@emotion/styled";
import MovieAutocomplete from "./movie-autocomplete";
import AppButton from "../../common/app-button";
import { Card, CardTitle } from "../../common/styling";

const Movie = styled(Card)`
  display: grid;
  grid-auto-rows: min-content;
  grid-template-columns: 1fr;
  justify-items: auto;
  grid-gap: 2rem;
`;

const MovieDescription = styled.p`
  font-family: "Roboto", sans-serif;
  font-size: 2rem;
  color: white;
  border-left: 10px solid white;
  padding-left: 1.5rem;
`;

const StyledHr = styled.hr`
  border: 0;
  height: 1px;
  background: #333;
  background-image: linear-gradient(to right, #ccc, #333, #ccc);
`;

/**
 * @deprecated Use Game.jsx from use-case/game instead
 */
class Guess extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      movieDescription: "This component is deprecated. Use the new Game component.",
    };
  }

  render() {
    return (
      <Movie>
        <CardTitle>Component Deprecated</CardTitle>
        <MovieDescription>
          {this.state.movieDescription}
        </MovieDescription>
        <StyledHr />
        <AppButton disabled>Use Game.jsx instead</AppButton>
      </Movie>
    );
  }
}

export default Guess;
