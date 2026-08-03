# ---- Build ----
FROM node:20-bookworm-slim AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

# ---- Serve ----
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
# SPA fallback so react-router deep links work
RUN echo 'server { listen 80; root /usr/share/nginx/html; index index.html; \
  location / { try_files $uri $uri/ /index.html; } }' > /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
