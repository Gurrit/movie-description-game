/**
 * CluePanel Component
 * 
 * Displays available clues and handles clue requests.
 * Shows which clues have been used and their point costs.
 */

import React, { useState } from "react";
import AppButton from "../../common/app-button";

// Clue type configuration
const CLUE_CONFIG = {
  YEAR: { label: "Year", cost: 50, disabledAfterGuess: false, color: "#3498db" },
  DIRECTOR: { label: "Director", cost: 100, disabledAfterGuess: false, color: "#27ae60" },
  ACTOR_1: { label: "Lead Actor", cost: 150, disabledAfterGuess: false, color: "#e74c3c" },
  ACTOR_2: { label: "Actor 2", cost: 200, disabledAfterGuess: false, color: "#c0392b" },
  ACTOR_3: { label: "Actor 3", cost: 250, disabledAfterGuess: false, color: "#a93226" },
  GENRE: { label: "Genre", cost: 75, disabledAfterGuess: false, color: "#8e44ad" },
  PLOT_HINT: { label: "Plot Hint", cost: 300, disabledAfterGuess: false, color: "#f39c12" },
  TITLE_HINT: { label: "Title Hint", cost: 500, disabledAfterGuess: false, color: "#d35400" },
};

const CHIP_COLORS = {
  YEAR: "#4a90e2",
  DIRECTOR: "#50c878",
  ACTOR_1: "#ff6b6b",
  ACTOR_2: "#ff6b6b",
  ACTOR_3: "#ff6b6b",
  GENRE: "#9b59b6",
  PLOT_HINT: "#f39c12",
  TITLE_HINT: "#e74c3c",
  default: "#95a5a6",
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
      <span
        key={clueType}
        style={{
          background: CHIP_COLORS[clueType] || CHIP_COLORS.default,
          color: "white",
          padding: "0.25rem 0.75rem",
          borderRadius: "1rem",
          fontSize: "0.875rem",
          fontFamily: "'Roboto', sans-serif",
          display: "inline-flex",
          alignItems: "center",
          gap: "0.25rem",
        }}
      >
        <span style={{ fontWeight: "bold" }}>-</span>
        {CLUE_CONFIG[clueType]?.label || clueType}
      </span>
    ));
  };

  return (
    <div className="p-4 mb-4" style={{ background: "rgba(0, 0, 0, 0.2)", borderRadius: "0.75rem" }}>
      <h3 style={{ fontFamily: "'Roboto', sans-serif", color: "white", marginBottom: "1rem", fontSize: "1.25rem" }}>
        Request a Clue
      </h3>

      <div className="d-flex flex-wrap gap-2 mb-3" style={{ minHeight: "2.5rem" }}>
        {renderUsedClues()}
      </div>

      <div className="d-grid gap-2" style={{ gridTemplateColumns: "repeat(auto-fill, minmax(140px, 1fr))" }}>
        {Object.entries(CLUE_CONFIG).map(([type, config]) => {
          const used = isClueUsed(type);
          const disabled = used || requesting === type;

          return (
            <AppButton
              key={type}
              onClick={() => handleRequestClue(type)}
              disabled={disabled}
              variant="primary"
              className="w-100"
              style={{
                padding: "0.75rem 1rem",
                fontSize: "0.875rem",
                background: disabled ? "#555" : config.color,
                border: "none",
                transition: "transform 0.1s ease, opacity 0.1s ease",
              }}
            >
              {config.label}
              <span style={{ fontSize: "0.75rem", opacity: "0.8", marginLeft: "0.25rem" }}>
                -{config.cost}
              </span>
              {requesting === type && "..."}
            </AppButton>
          );
        })}
      </div>
    </div>
  );
}
