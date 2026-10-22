# Frontend

## Stack
TypeScript (strict), React 18+, Vite, **pnpm** (pin through `packageManager` in `package.json`; commit `pnpm-lock.yaml`; remove `yarn.lock` and react-scripts/CRA), Tailwind CSS, TanStack Query, React Router, `openapi-fetch` + `openapi-typescript`.

## Structure
```
app/src/
  api/         schema.d.ts (generated), client.ts (openapi-fetch + auth middleware), queries.ts (query/mutation hooks)
  features/
    game/      GamePage, RoundView, ClueList, GuessInput (autocomplete), RoundResult, GameSummary
    stats/     StatsPage
    player/    token storage, bootstrap
  components/  shared UI
  routes.tsx   router: "/" (game), "/stats"
  main.tsx
```

## Player bootstrap and token
- Token is kept in `localStorage` under one key. On startup: if absent, call `POST /players` and store the response. All storage access is wrapped in try/catch (private mode); fall back to an in-memory token with a visible notice that progress won't survive a reload.
- The `openapi-fetch` middleware adds the `Authorization` header. On a 401, the token is discarded, a new player created, and the failed request is retried once.

## Server state with TanStack Query
- The **server is the only source of truth** for game state. No game rules in the client (attempt counting, correctness, round advance): it renders what the API returns.
- `useGame()` → `GET /game/today`, with `404` meaning "not started" (shown as a Start screen). Mutations (`start`, `reveal`, `guess`) return the updated game, which is written to the query cache via `setQueryData`. Disable the submit button while a mutation is pending to avoid double submit.
- Autocomplete: `useQuery` on `/movies/search`, debounced 250 ms, min 2 chars, `keepPreviousData`, results already guessed this round are disabled.
- Retry policy: queries retry twice with backoff; mutations do not retry automatically.
- The Start screen and loading states must tolerate slow responses (backends may cold start or be generating the daily set).

## UI/UX requirements
- Mobile-first layout with Tailwind; keyboard-accessible autocomplete (combobox pattern, ARIA); visible attempt counter and progress (round i of N).
- Clues: all clue slots are listed; unrevealed ones display a "Reveal" action; redaction placeholder `▇▇▇` renders as-is.
- Round end shows the answer (poster via TMDB image base URL from a config constant, e.g. `https://image.tmdb.org/t/p/w342`) before a "Next" action; final screen shows the game summary and a link to stats.
- Poster URL base is a build-time constant (`VITE_TMDB_IMAGE_BASE`), not fetched at runtime.

## Build and dev
- `pnpm dev` (Vite dev server with `server.proxy` forwarding `/api` to the backend), `pnpm build`, `pnpm test`, `pnpm generate:api`, `pnpm typecheck`, `pnpm lint` (ESLint + Prettier).
- Production build is static files. The host must serve `index.html` for unknown paths (SPA fallback) so `/stats` works on reload.
