# === UI (Vite React) Dockerfile ===
# Multi-stage: build static assets with Node, then serve with Nginx

# 1) BUILD STAGE
FROM node:20-alpine AS ui-build
WORKDIR /app

# Install deps first for better caching
COPY package*.json ./
RUN npm ci --no-audit --no-fund

# Copy source
COPY . .

# Build-time API base configuration; default to "/api" to work with nginx proxy
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

# Build static files
RUN npm run build

# 2) RUNTIME STAGE
FROM nginx:1.27-alpine AS ui-runtime

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built assets
COPY --from=ui-build /app/dist /usr/share/nginx/html

EXPOSE 80

# Health check could be defined in compose; keeping image minimal here.
