# Build stage
FROM node:20-alpine as BUILD
WORKDIR /usr/src/app

# Copy package files first for better caching
COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile --production

# Copy source files
COPY ./src ./src
COPY ./public ./public

# Build the app
RUN yarn build

# Production stage - use nginx to serve the built app
FROM nginx:1.25-alpine

# Copy nginx config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built files from build stage
COPY --from=BUILD /usr/src/app/build/ /usr/share/nginx/html/

# Expose port
EXPOSE 3000

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
