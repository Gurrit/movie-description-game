/**
 * Loading Component
 * 
 * Displays a loading spinner with optional message.
 */

import React from "react";
import styled from "@emotion/styled";
import { keyframes } from "@emotion/react";

// Animation
const spin = keyframes`
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
`;

const pulse = keyframes`
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
`;

// Styled Components
const LoadingContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  min-height: 200px;
`;

const Spinner = styled.div`
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top-color: #3498db;
  border-radius: 50%;
  animation: ${spin} 1s linear infinite;
  margin-bottom: 1rem;
`;

const LoadingMessage = styled.p`
  font-family: "Roboto", sans-serif;
  color: #bdc3c7;
  font-size: 1rem;
  animation: ${pulse} 1.5s ease-in-out infinite;
`;

/**
 * Loading Component
 * 
 * @param {Object} props
 * @param {string} props.message - Loading message to display
 * @param {boolean} props.fullPage - Whether to center on full page
 */
export default function Loading({ message = "Loading...", fullPage = false }) {
  return (
    <LoadingContainer style={fullPage ? { minHeight: "100vh" } : {}}>
      <Spinner />
      <LoadingMessage>{message}</LoadingMessage>
    </LoadingContainer>
  );
}
