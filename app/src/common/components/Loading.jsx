/**
 * Loading Component
 * 
 * Displays a loading spinner with optional message.
 */

import React from "react";
import { Spinner } from "react-bootstrap";

/**
 * Loading Component
 * 
 * @param {Object} props
 * @param {string} props.message - Loading message to display
 * @param {boolean} props.fullPage - Whether to center on full page
 */
export default function Loading({ message = "Loading...", fullPage = false }) {
  return (
    <div 
      className="d-flex flex-column align-items-center justify-content-center p-5"
      style={fullPage ? { minHeight: "100vh" } : { minHeight: "200px" }}
    >
      <Spinner 
        animation="border" 
        role="status"
        variant="primary"
        style={{ width: "3rem", height: "3rem", marginBottom: "1rem" }}
      >
        <span className="visually-hidden">Loading...</span>
      </Spinner>
      <p style={{ fontFamily: "'Roboto', sans-serif", color: "#bdc3c7", fontSize: "1rem" }}>
        {message}
      </p>
    </div>
  );
}
