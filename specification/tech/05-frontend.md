# Frontend

## Stack
TypeScript (strict), React 19, Vite, **pnpm** (pinned through `packageManager`; `pnpm-lock.yaml` committed), Tailwind CSS v4 (`@tailwindcss/vite`, dark mode follows the OS), TanStack Query v5, React Router v7 (`react-router-dom`), `openapi-fetch` + `openapi-typescript`. Tests: Vitest, Testing Library, MSW.

**TypeScript is pinned to 5.9.x**: `openapi-typescript` and `typescript-eslint` use TypeScript's JavaScript API, which the native TypeScript 7 does not provide.

## Structure
```
app/src/
  api/         schema.d.ts (generated, git-ignored), types.ts (aliases of schema types), ApiError.ts,
               client.ts (openapi-fetch + token handling), queries.ts (TanStack Query hooks)
  features/
    player/    token.ts (localStorage with in-memory fallback)
    game/      GamePage, StartScreen, ActiveGame, RoundView, RoundResult, GameSummary, ClueList,
               ClueContentView, GuessInput (autocomplete), ProgressDots, NextGameCountdown
    stats/     StatsPage
  components/  Layout, Button, Poster, Spinner, ErrorMessage
  test/        MSW server, an in-memory fake of the API (fakeBackend.ts), render helpers
  routes.tsx   "/" (game) and "/stats"
```

## Player bootstrap and token
- The token is kept in `localStorage` (`mdg.token`). Storage access is wrapped in try/catch; if it fails the token lives in memory and the layout shows a notice that progress will not survive a reload.
- `client.ts` passes a custom `fetch` to `openapi-fetch`. It adds `Authorization: Bearer <token>` to every call; if there is no token it first calls `POST /players` (concurrent callers share one request). `POST /players` itself is sent without a token.
- On a 401 the stored token is discarded, a new player is created and the request is retried **once** (body included). A second 401 is returned to the caller, so a broken server cannot cause a loop.
- The API base is `${location.origin}/api` (absolute, so tests work too). Dev: Vite proxies `/api` to `localhost:8080`; production: the reverse proxy.

## Server state with TanStack Query
- The **server is the only source of truth** for game state. The client has no game rules (attempt counting, correctness, round advance): it renders what the API returns.
- `useTodaysGame()` returns the game, or `null` for 404 `GAME_NOT_STARTED` (which shows the start screen). `useStartGame`, `useRevealClue` and `useSubmitGuess` are mutations that write the returned `Game` into the cache with `setQueryData`; a 409 invalidates the game query so the real state is shown.
- The only game-related client state is `ActiveGame`'s count of *ended rounds already looked at*. When a guess ends a round, the API already moves the game on, so the result screen of the ended round is shown until the player presses Next. It is initialised from the loaded game, so a reload never replays old results and a finished game opens on the summary.
- Autocomplete: `useMovieSearch` (min 2 characters, 250 ms debounce, `keepPreviousData`, 10 minute stale time); movies already guessed in the round are shown but cannot be picked.
- Retry policy: queries retry twice on 5xx/network errors and never on 4xx; mutations do not retry.
- Slow responses are expected (cold start, first game of the day generating the set): the start button shows "Preparing today's game…".

## UI/UX
Clean, Wordle-like, mobile-first. The layout is a **narrow centred column** (`max-w-xl`, 36rem) at every screen size, so desktop looks like a wider phone.

- **Palette:** neutral slate, with green (solved), red (failed / used attempt) and amber (current movie) as the only colours. The primary button is the dark/light inverse of the page; light and dark themes follow the OS. System font stack, no web fonts.
- **Progress:** one numbered square per movie, coloured by round status (`ProgressSquares`). Attempts are squares too: used ones filled red, remaining ones outlined (`AttemptSquares`), next to the text "2 attempts left".
- **Clues:** stacked cards. An unrevealed clue is a dashed, full-width button (min height 64px) labelled "Tap to reveal", with the accessible name "Reveal plot" etc.; a revealed one is a solid card that fades in (skipped for `prefers-reduced-motion`). The `▇▇▇` redaction mask renders as-is.
- **Guess box and Next button:** pinned to the bottom of the screen on phones (`BottomBar`, respects the safe area) and inline from the `sm` breakpoint up. The input uses 16px text (prevents iOS focus-zoom) and the suggestion list opens **upward** on phones so the keyboard does not cover it. Suggestion rows are at least 48px high.
- **Round end:** a full-width green or red banner with the poster and title, then all four clues, then the Next button. The summary shows the score and one row per movie (square, title, attempts), plus a countdown to the next UTC midnight.
- **Icon:** the clapperboard is an inline SVG. Emoji are avoided because rendering depends on the device's emoji font.
- Poster URL base is a build-time constant (`VITE_TMDB_IMAGE_BASE`), not fetched at runtime.
- Not yet done: a real name, logo and favicon (the title is a placeholder); sharing results.

## Visual checks
Screenshots can be taken with headless Chromium by stubbing `/api/*` (`page.route` on pathnames starting with `/api/`, **not** the pattern `**/api/**`, which also matches Vite's `/src/api/*` modules). Checked at 360px, 390px and 1280px, light and dark. The Playwright e2e in phase 5 should include a mobile-viewport project.

## Build and dev
- `pnpm dev` (Vite dev server with `server.proxy` forwarding `/api` to the backend), `pnpm build`, `pnpm test`, `pnpm generate:api`, `pnpm typecheck`, `pnpm lint` (ESLint + Prettier).
- Production build is static files. The host must serve `index.html` for unknown paths (SPA fallback) so `/stats` works on reload.
