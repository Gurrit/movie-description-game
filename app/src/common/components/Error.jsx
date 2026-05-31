/**
 * Error Component
 * 
 * Displays error messages with retry and back options.
 */

import React from "react";
import styled from "@emotion/styled";
import AppButton from "../app-button";

// Styled Components
const ErrorContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  min-height: 200px;
  text-align: center;
`;

const ErrorIcon = styled.div`
  font-size: 3rem;
  color: #e74c3c;
  margin-bottom: 1rem;
`;

const ErrorTitle = styled.h3`
  font-family: "Roboto", sans-serif;
  color: #ecf0f1;
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
`;

const ErrorMessage = styled.p`
  font-family: "Roboto", sans-serif;
  color: #95a5a6;
  font-size: 1rem;
  margin-bottom: 2rem;
  max-width: 400px;
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 1rem;
`;

/**
 * Error Component
 * 
 * @param {Object} props
 * @param {string} props.message - Error message to display
 * @param {Function} props.onRetry - Callback for retry action
 * @param {Function} props.onBack - Callback for back action
 * @param {boolean} props.fullPage - Whether to center on full page
 */
export default function Error({ message = "An error occurred", onRetry, onBack, fullPage = false }) {
  return (
    <ErrorContainer style={fullPage ? { minHeight: "100vh" } : {}}>
      <ErrorIcon>⚠️</ErrorIcon>
      <ErrorTitle>Something went wrong</ErrorTitle>
      <ErrorMessage>{message}</ErrorMessage>
      
      <ButtonGroup>
        {onRetry && (
          <AppButton onClick={onRetry}>
            Try Again
          </AppButton>
        )}
        {onBack && (
          <AppButton onClick={onBack} variant="secondary">
            Go Back
          </AppButton>
        )}
      </ButtonGroup>
    </ErrorContainer>
  );
}
