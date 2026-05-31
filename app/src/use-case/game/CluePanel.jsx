/**
 * CluePanel Component
 * 
 * Displays available clues and handles clue requests.
 * Shows which clues have been used and their point costs.
 */

import React, { useState } from "react";
import styled from "@emotion/styled";
import AppButton from "../../common/app-button";

// Styled Components
const CluePanelContainer = styled.div`
  background: rgba(0, 0, 0, 0.2);
  padding: 1.5rem;
  border-radius: 0.75rem;
  margin-bottom: 1.5rem;
`;

const CluePanelTitle = styled.h3`
  font-family: "Roboto", sans-serif;
  color: white;
  margin-bottom: 1rem;
  font-size: 1.25rem;
`;

const CluesUsedContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
  min-height: 2.5rem;
`;

const ClueChip = styled.span`
  background: ${(props) => {
    switch (props.type) {
      case "YEAR": return "#4a90e2";
      case "DIRECTOR": return "#50c878";
      case "ACTOR_1":
      case "ACTOR_2":
      case "ACTOR_3": return "#ff6b6b";
      case "GENRE": return "#9b59b6";
      case "PLOT_HINT": return "#f39c12";
      case "TITLE_HINT": return "#e74c3c";
      default: return "#95a5a6";
    }
  }};
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  font-size: 0.875rem;
  font-family: "Roboto", sans-serif;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;

  &::before {
    content: "-";
    font-weight: bold;
  }
`;

const ClueButtonsContainer = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.75rem;
`;

const ClueButton = styled(AppButton)`
  width: 100%;
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  background: ${(props) => {
    switch (props.cluetype) {
      case "YEAR": return "#3498db";
      case "DIRECTOR": return "#27ae60";
      case "ACTOR_1": return "#e74c3c";
      case "ACTOR_2": return "#c0392b";
      case "ACTOR_3": return "#a93226";
      case "GENRE": return "#8e44ad";
      case "PLOT_HINT": return "#f39c12";
      case "TITLE_HINT": return "#d35400";
      default: return "#7f8c8d";
    }
  }};
  border: none;
  transition: transform 0.1s ease, opacity 0.1s ease;

  &:hover:not(:disabled) {
    transform: scale(1.05);
    opacity: 0.9;
  }

  &:disabled {
    background: #555;
    cursor: not-allowed;
    opacity: 0.6;
  }
`;

const ClueCost = styled.span`
  font-size: 0.75rem;
  opacity: 0.8;
  margin-left: 0.25rem;
`;

// Clue type configuration
const CLUE_CONFIG = {
  YEAR: { label: "Year", cost: 50, disabledAfterGuess: false },
  DIRECTOR: { label: "Director", cost: 100, disabledAfterGuess: false },
  ACTOR_1: { label: "Lead Actor", cost: 150, disabledAfterGuess: false },
  ACTOR_2: { label: "Actor 2", cost: 200, disabledAfterGuess: false },
  ACTOR_3: { label: "Actor 3", cost: 250, disabledAfterGuess: false },
  GENRE: { label: "Genre", cost: 75, disabledAfterGuess: false },
  PLOT_HINT: { label: "Plot Hint", cost: 300, disabledAfterGuess: false },
  TITLE_HINT: { label: "Title Hint", cost: 500, disabledAfterGuess: false },
};

/**
 * CluePanel Component
 * 
 * @param {Object} props
 * @param {string} props.sessionId - Current session ID
 * @param {Array<string>} props.usedClues - Clues already used for current movie
 * @param {Function} props.onRequestClue - Callback when clue is requested
 */
export default function CluePanel({ sessionId, usedClues = [], onRequestClue }) {
  const [requesting, setRequesting] = useState(null);

  // Check if a clue type has been used
  const isClueUsed = (clueType) => {
    return usedClues.includes(clueType);
  };

  // Handle clue request
  const handleRequestClue = async (clueType) => {
    if (!onRequestClue || isClueUsed(clueType)) return;

    setRequesting(clueType);
    try {
      await onRequestClue(clueType);
    } finally {
      setRequesting(null);
    }
  };

  // Render used clues
  const renderUsedClues = () => {
    if (usedClues.length === 0) {
      return <span style={{ color: "#95a5a6", fontSize: "0.875rem" }}>No clues used yet</span>;
    }

    return usedClues.map((clueType) => (
      <ClueChip key={clueType} type={clueType}>
        {CLUE_CONFIG[clueType]?.label || clueType}
      </ClueChip>
    ));
  };

  return (
    <CluePanelContainer>
      <CluePanelTitle>Request a Clue</CluePanelTitle>

      <CluesUsedContainer>
        {renderUsedClues()}
      </CluesUsedContainer>

      <ClueButtonsContainer>
        {Object.entries(CLUE_CONFIG).map(([type, config]) => {
          const used = isClueUsed(type);
          const disabled = used || requesting === type;

          return (
            <ClueButton
              key={type}
              onClick={() => handleRequestClue(type)}
              disabled={disabled}
              cluetype={type}
            >
              {config.label}
              <ClueCost>-{config.cost}</ClueCost>
              {requesting === type && "..."}
            </ClueButton>
          );
        })}
      </ClueButtonsContainer>
    </CluePanelContainer>
  );
}
