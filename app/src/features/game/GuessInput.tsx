import { useId, useState } from 'react';
import { MIN_SEARCH_LENGTH, useMovieSearch } from '../../api/queries';
import type { MovieSuggestion } from '../../api/types';
import { errorText } from '../../components/ErrorMessage';
import { useDebouncedValue } from './useDebouncedValue';

export const SEARCH_DEBOUNCE_MS = 250;

type Props = {
  /** TMDB ids already guessed in this round; they cannot be picked again. */
  guessedIds: ReadonlySet<number>;
  disabled?: boolean;
  onGuess: (movie: MovieSuggestion) => void;
};

/** Movie title autocomplete following the ARIA combobox pattern. A guess is always a picked movie. */
export function GuessInput({ guessedIds, disabled = false, onGuess }: Props) {
  const [text, setText] = useState('');
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const listId = useId();
  const optionId = (index: number) => `${listId}-option-${index}`;

  const query = useDebouncedValue(text.trim(), SEARCH_DEBOUNCE_MS);
  const searching = query.length >= MIN_SEARCH_LENGTH;
  const search = useMovieSearch(query);
  const results = searching ? (search.data ?? []) : [];
  const showList = open && text.trim().length >= MIN_SEARCH_LENGTH;

  const pick = (movie: MovieSuggestion) => {
    if (disabled || guessedIds.has(movie.tmdbId)) return;
    setText('');
    setOpen(false);
    setActive(0);
    onGuess(movie);
  };

  const onKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setOpen(true);
      setActive((i) => Math.min(i + 1, results.length - 1));
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      setActive((i) => Math.max(i - 1, 0));
    } else if (event.key === 'Enter' && showList && results[active]) {
      event.preventDefault();
      pick(results[active]);
    } else if (event.key === 'Escape') {
      setOpen(false);
    }
  };

  return (
    <div className="relative">
      <label htmlFor={`${listId}-input`} className="sr-only">
        Your guess
      </label>
      <input
        id={`${listId}-input`}
        type="text"
        role="combobox"
        aria-expanded={showList}
        aria-controls={listId}
        aria-autocomplete="list"
        aria-activedescendant={showList && results[active] ? optionId(active) : undefined}
        autoComplete="off"
        placeholder="Guess the movie…"
        disabled={disabled}
        value={text}
        onChange={(event) => {
          setText(event.target.value);
          setOpen(true);
          setActive(0);
        }}
        onKeyDown={onKeyDown}
        onBlur={() => setOpen(false)}
        // 16px+ text stops iOS from zooming the page when the field gets focus
        className="min-h-12 w-full rounded-lg border-2 border-slate-300 bg-white px-4 py-2 text-base focus:border-slate-900 focus:outline-none disabled:opacity-50 dark:border-slate-600 dark:bg-slate-900 dark:focus:border-white"
      />
      {showList && (
        <ul
          id={listId}
          role="listbox"
          aria-label="Movie suggestions"
          // Opens upward on phones, where the box is pinned to the bottom of the screen
          className="absolute bottom-full z-10 mb-1 max-h-[50dvh] w-full overflow-auto rounded-lg border-2 border-slate-300 bg-white shadow-lg sm:top-full sm:bottom-auto sm:mt-1 sm:mb-0 dark:border-slate-600 dark:bg-slate-900"
        >
          {search.isError && (
            <li role="presentation" className="px-4 py-3 text-sm text-red-700 dark:text-red-300">
              {errorText(search.error)}
            </li>
          )}
          {!search.isError && search.isFetching && results.length === 0 && (
            <li role="presentation" className="px-4 py-3 text-sm text-slate-500">
              Searching…
            </li>
          )}
          {!search.isError && !search.isFetching && searching && results.length === 0 && (
            <li role="presentation" className="px-4 py-3 text-sm text-slate-500">
              No matches
            </li>
          )}
          {results.map((movie, index) => {
            const guessed = guessedIds.has(movie.tmdbId);
            return (
              <li
                key={movie.tmdbId}
                id={optionId(index)}
                role="option"
                aria-selected={index === active}
                aria-disabled={guessed}
                // mousedown, not click: the input's blur would close the list before a click lands
                onMouseDown={(event) => {
                  event.preventDefault();
                  pick(movie);
                }}
                className={`min-h-12 cursor-pointer px-4 py-3 ${index === active ? 'bg-slate-100 dark:bg-slate-800' : ''} ${
                  guessed ? 'cursor-not-allowed text-slate-400 line-through' : ''
                }`}
              >
                {movie.title}
                {movie.year != null && <span className="text-slate-500"> ({movie.year})</span>}
                {guessed && <span className="ml-2 text-xs no-underline">already guessed</span>}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
