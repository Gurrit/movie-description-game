/**
 * Error Component
 * 
 * Displays error messages with retry and back options.
 */

import React from "react";
import AppButton from "../app-button";

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
    <div 
      className="d-flex flex-column align-items-center justify-content-center p-5 text-center"
      style={fullPage ? { minHeight: "100vh" } : { minHeight: "200px" }}
    >
      <div style={{ fontSize: "3rem", color: "#e74c3c", marginBottom: "1rem" }}>
        ⚠️
      </div>
      <h3 style={{ fontFamily: "'Roboto', sans-serif", color: "#ecf0f1", fontSize: "1.5rem", marginBottom: "0.5rem" }}>
        Something went wrong
      </h3>
      <p style={{ fontFamily: "'Roboto', sans-serif", color: "#95a5a6", fontSize: "1rem", marginBottom: "2rem", maxWidth: "400px" }}>
        {message}
      </p>
      
      <div className="d-flex gap-3">
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
      </div>
    </div>
  );
}
