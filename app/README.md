# Frontend

React + TypeScript SPA built with Vite and pnpm. See `../specification/tech/05-frontend.md`.

```sh
pnpm install
pnpm dev          # http://localhost:3000, proxies /api to the backend on :8080
pnpm test         # Vitest + Testing Library + MSW
pnpm lint
pnpm typecheck
pnpm build
```

`src/api/schema.d.ts` is generated from `../api/openapi.yaml` (`pnpm generate:api`, run automatically
before dev, build, test and typecheck) and is not committed.
