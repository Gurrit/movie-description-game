import CircularProgress from "@material-ui/core/CircularProgress";
import Autocomplete from "@material-ui/core/Autocomplete";
import TextField from "@material-ui/core/TextField";
import { useEffect, useState } from "react";

/**
 * MovieAutocomplete Component
 * 
 * Autocomplete input for selecting movies from a list.
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
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [options, setOptions] = useState([]);
  const loading = open && options.length === 0;

  // Filter movies based on search input
  useEffect(() => {
    if (open && search) {
      const filtered = movies.filter((movie) =>
        movie.title.toLowerCase().includes(search.toLowerCase()) ||
        movie.originalTitle?.toLowerCase().includes(search.toLowerCase())
      );
      setOptions(filtered);
    } else if (open) {
      setOptions(movies);
    }
  }, [open, search, movies]);

  useEffect(() => {
    if (!open) {
      setOptions([]);
    }
  }, [open]);

  // Map movie object to display format
  const getOptionLabel = (option) => {
    if (!option) return "";
    if (typeof option === "string") return option;
    return `${option.title} (${option.year})`;
  };

  return (
    <Autocomplete
      open={open}
      onOpen={() => {
        setOpen(true);
      }}
      onClose={() => {
        setOpen(false);
      }}
      getOptionSelected={(option, value) => option.id === value?.id}
      getOptionLabel={getOptionLabel}
      options={options}
      loading={loading}
      value={selectedMovie}
      onChange={(e, newValue) => {
        if (onSelect) {
          onSelect(newValue);
        }
      }}
      disabled={disabled}
      renderInput={(params) => (
        <TextField
          {...params}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          label={placeholder}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading ? (
                  <CircularProgress color="inherit" size={20} />
                ) : null}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
    />
  );
};

export default MovieAutocomplete;
