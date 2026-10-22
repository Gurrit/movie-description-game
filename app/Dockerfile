# The web image: the built SPA served by Caddy, which also proxies /api to the backend.
# Build with the repository root as context: the API contract in /api is needed to generate types.
#   docker build -f app/Dockerfile .

FROM node:22-alpine AS build
RUN corepack enable
WORKDIR /workspace/app
COPY app/package.json app/pnpm-lock.yaml ./
RUN --mount=type=cache,target=/root/.local/share/pnpm/store pnpm install --frozen-lockfile
COPY api /workspace/api
COPY app/ .
# Optional: where poster images come from (defaults to TMDB's image CDN)
ARG VITE_TMDB_IMAGE_BASE=""
ENV VITE_TMDB_IMAGE_BASE=$VITE_TMDB_IMAGE_BASE
RUN pnpm build

FROM caddy:2-alpine
COPY app/Caddyfile /etc/caddy/Caddyfile
COPY --from=build /workspace/app/dist /srv
