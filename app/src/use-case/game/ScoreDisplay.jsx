/**
 * ScoreDisplay Component
 * 
 * Shows current score, total possible, and percentage.
 */

import React from "react";
import styled from "@emotion/styled";

// Styled Components
const ScoreContainer = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem 1rem;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 0.5rem;
`;

const ScoreLabel = styled.span`
  font-family: "Roboto", sans-serif;
  color: #7f8c8d;
  font-size: 0.875rem;
  font-weight: 500;
`;

const ScoreValue = styled.span`
  font-family: "Roboto", sans-serif;
  color: ${(props) => {
    const percentage = props.percentage || 0;
    if (percentage >= 80) return "#2ecc71";
    if (percentage >= 50) return "#f39c12";
    if (percentage >= 20) return "#e74c3c";
    return "#95a5a6";
  }};
  font-size: 1.25rem;
  font-weight: bold;
`;

const ScoreBarContainer = styled.div`
  width: 120px;
  height: 10px;
  background: #34495e;
  border-radius: 5px;
  overflow: hidden;
`;

const ScoreBar = styled.div`
  height: 100%;
  width: ${(props) => props.percentage}%;
  background: ${(props) => {
    const percentage = props.percentage || 0;
    if (percentage >= 80) return "#2ecc71";
    if (percentage >= 50) return "#f39c12";
    if (percentage >= 20) return "#e74c3c";
    return "#95a5a6";
  }};
  border-radius: 5px;
  transition: width 0.3s ease;
`;

/**
 * ScoreDisplay Component
 * 
 * @param {Object} props
 * @param {number} props.score - Current score
 * @param {number} props.total - Total possible score
 * @param {number} props.percentage - Score as percentage (0-100)
 */
export default function ScoreDisplay({ score = 0, total = 5000, percentage = 0 }) {
  return (
    <ScoreContainer>
      <ScoreLabel>Score:</ScoreLabel>
      <ScoreValue percentage={percentage}>
        {score} / {total}
      </ScoreValue>
      <ScoreBarContainer>
        <ScoreBar percentage={percentage} />
      </ScoreBarContainer>
    </ScoreContainer>
  );
}
