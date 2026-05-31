import React from "react";
import { Routes, Route } from "react-router-dom";
import Home from "../use-case/home";
import Game from "../use-case/game/Game";
import Results from "../use-case/results/Results";

/**
 * Main App Component
 * 
 * Handles routing between Home, Game, and Results pages.
 * Manages global layout and header with Bootstrap styling.
 */
const App = () => {
  return (
    <div style={{ minHeight: "100vh", background: "linear-gradient(to bottom, #16213e 0%, #0f3460 100%)", color: "#ecf0f1" }}>
      <header style={{ padding: "2rem", background: "linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)", boxShadow: "0 2px 10px rgba(0, 0, 0, 0.3)" }}>
        <h1 style={{ fontFamily: "'Bebas Neue', cursive", color: "#05c46b", fontSize: "5rem", textAlign: "center", margin: "0", textShadow: "0 2px 4px rgba(0, 0, 0, 0.5)" }}>
          Movie Description Game
        </h1>
      </header>
      <main className="container" style={{ maxWidth: "1200px", padding: "0 1rem 2rem" }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/play/:sessionId/results" element={<Results />} />
          <Route path="/play/:sessionId?" element={<Game />} />
        </Routes>
      </main>
    </div>
  );
};

export default App;
