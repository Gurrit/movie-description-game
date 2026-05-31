import React, { useEffect, useState } from "react";
import { Form, Dropdown } from "react-bootstrap";

/**
 * MovieAutocomplete Component
 * 
 * Autocomplete input for selecting movies from a list using Bootstrap.
 * 
 * @param {Object} props
 * @param {Array<Object>} props.movies - List of movies to select from
 * @param {Object} props.selectedMovie - Currently selected movie
 * @param {Function} props.onSelect - Callback when movie is selected
 * @param {boolean} props.disabled - Whether the input is disabled
 * @param {string} props.placeholder - Placeholder text
 */
const MovieAutocomplete = ({
  movies = [],
  selectedMovie = null,
  onSelect,
  disabled = false,
  placeholder = "Search movies...",
}) => {
  const [search, setSearch] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);
  const [filteredMovies, setFilteredMovies] = useState([]);

  // Filter movies based on search input
  useEffect(() => {
    if (search) {
      const filtered = movies.filter((movie) =>
        movie.title.toLowerCase().includes(search.toLowerCase()) ||
        movie.originalTitle?.toLowerCase().includes(search.toLowerCase())
      );
      setFilteredMovies(filtered);
    } else {
      setFilteredMovies(movies);
    }
  }, [search, movies]);

  // Map movie object to display format
  const getOptionLabel = (option) => {
    if (!option) return "";
    if (typeof option === "string") return option;
    return `${option.title} (${option.year})`;
  };

  const handleSelect = (movie) => {
    setSearch(getOptionLabel(movie));
    setShowDropdown(false);
    if (onSelect) {
      onSelect(movie);
    }
  };

  const handleFocus = () => {
    setShowDropdown(true);
  };

  const handleBlur = () => {
    // Delay hiding to allow click on dropdown item
    setTimeout(() => setShowDropdown(false), 200);
  };

  return (
    <Dropdown show={showDropdown} onToggle={(isOpen) => setShowDropdown(isOpen)}>
      <Form.Control
        type="text"
        value={search}
        onChange={(e) => {
          setSearch(e.target.value);
          setShowDropdown(true);
        }}
        onFocus={handleFocus}
        onBlur={handleBlur}
        placeholder={placeholder}
        disabled={disabled}
        autoComplete="off"
      />
      <Dropdown.Menu show={showDropdown} style={{ maxHeight: "300px", overflowY: "auto", width: "100%" }}>
        {filteredMovies.length > 0 ? (
          filteredMovies.map((movie) => (
            <Dropdown.Item
              key={movie.id}
              onClick={() => handleSelect(movie)}
              active={selectedMovie?.id === movie.id}
            >
              {getOptionLabel(movie)}
            </Dropdown.Item>
          ))
        ) : (
          <Dropdown.Item disabled>No movies found</Dropdown.Item>
        )}
      </Dropdown.Menu>
    </Dropdown>
  );
};

export default MovieAutocomplete;
