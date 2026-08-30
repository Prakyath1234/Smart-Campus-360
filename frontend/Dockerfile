# Stage 1: Build the React application
FROM node:20-alpine AS build
WORKDIR /app/frontend

# Create target directory for build output as defined in vite.config.js
RUN mkdir -p /app/backend/src/main/resources/static

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve the build directory with Nginx
FROM nginx:alpine
COPY --from=build /app/backend/src/main/resources/static /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
