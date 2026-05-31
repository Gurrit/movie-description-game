/**
 * ScoreDisplay Component
 * 
 * Shows current score, total possible, and percentage.
 */

import React from "react";

/**
 * ScoreDisplay Component
 * 
 * @param {Object} props
 * @param {number} props.score - Current score
 * @param {number} props.total - Total possible score
 * @param {number} props.percentage - Score as percentage (0-100)
 */
const getColor = (percentage) => {
  if (percentage >= 80) return "#2ecc71";
  if (percentage >= 50) return "#f39c12";
  if (percentage >= 20) return "#e74c3c";
  return "#95a5a6";
};

export default function ScoreDisplay({ score = 0, total = 5000, percentage = 0 }) {
  return (
    <div className="d-flex align-items-center gap-3 p-2" style={{ background: "rgba(0, 0, 0, 0.3)", borderRadius: "0.5rem" }}>
      <span style={{ fontFamily: "'Roboto', sans-serif", color: "#7f8c8d", fontSize: "0.875rem", fontWeight: "500" }}>
        Score:
      </span>
      <span style={{ fontFamily: "'Roboto', sans-serif", color: getColor(percentage), fontSize: "1.25rem", fontWeight: "bold" }}>
        {score} / {total}
      </span>
      <div style={{ width: "120px", height: "10px", background: "#34495e", borderRadius: "5px", overflow: "hidden" }}>
        <div 
          style={{ 
            height: "100%", 
            width: `${percentage}%`, 
            background: getColor(percentage), 
            borderRadius: "5px",
            transition: "width 0.3s ease"
          }}
        />
      </div>
    </div>
  );
}
