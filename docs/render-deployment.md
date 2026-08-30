# Render Cloud Production Deployment Guide (External MySQL)

This guide describes how to deploy the SmartCampus 360 suite to Render, using an **external MySQL-compatible database** to avoid Render paid private database services or persistent disk costs.

---

## 1. Prerequisites
- A verified [Render.com](https://render.com) account.
- The codebase pushed to a private or public GitHub repository.
- An externally hosted MySQL database instance (e.g. from an external MySQL cloud provider or host).

---

## 2. Infrastructure Overview
Render orchestrates the application using two web services:

```
        [ Client Browser ]
                ↓ (Public HTTPS URL)
   [ Web Service: smart-campus-frontend ]
    └─ Env: Nginx + Compiled React SPA
    └─ Port: 80
                ↓ (Proxy Pass via BACKEND_URL)
    [ Web Service: smart-campus-backend ]
     └─ Env: Java 21 + LibreOffice
     └─ Port: $PORT (Dynamic)
     └─ Health Check: /actuator/health
                ↓ (Remote/SSL Connection)
      [ External MySQL Database ]
       └─ Env: MySQL 8.0 Compatible Instance
```

### External MySQL Requirements
Your external database provider must support:
- MySQL 8.0 compatibility.
- Remote connections from external IPs (Render's backend outbound IPs).
- SSL/TLS configurations (configured dynamically via `DB_SSL`).
- Persistent storage hosted on the provider side.
- Valid database credentials (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`).

---

## 3. Deploying using Render Blueprints (`render.yaml`)

1. Navigate to the Render Dashboard.
2. Click **New** -> **Blueprint**.
3. Connect your GitHub repository containing the codebase.
4. Render will auto-detect the [`render.yaml`](file:///f:/360/Smart-Campus-360/render.yaml) configuration.
5. Render will prompt you to enter the environment variables for your external database:
   - `DB_HOST`: Hostname of your external database (e.g., `mysql.example.com`).
   - `DB_USERNAME`: Username for database connection.
   - `DB_PASSWORD`: Password for database user.
6. Click **Apply** to deploy the frontend and backend services.

---

## 4. Manual Deployment Setup (Alternative)

If you prefer to configure the services manually via the Render GUI, use the following specifications:

### A. Backend (Web Service)
- **Service Type**: Web Service
- **Name**: `smart-campus-backend`
- **Runtime**: `Docker`
- **Root Directory**: `backend`
- **Docker Command**: Runs automatically from Dockerfile ENTRYPOINT.
- **Port**: Bind to Render's default dynamic `$PORT`.
- **Health Check Path**: `/actuator/health`
- **Environment Variables**:
  - `SPRING_PROFILES_ACTIVE`: `mysql`
  - `DB_HOST`: *(Your external database hostname)*
  - `DB_PORT`: `3306` (or your custom provider port)
  - `DB_NAME`: `smart_campus_360`
  - `DB_USERNAME`: *(Your external database username)*
  - `DB_PASSWORD`: *(Your external database password)*
  - `DB_SSL`: `false` (set to `true` if your database provider requires SSL)
  - `JWT_SECRET`: *(Generate secure 256-bit Hex string)*
  - `SMARTTOOLS_LIBREOFFICE_PATH`: `/usr/bin/soffice`

### B. Frontend (Web Service)
- **Service Type**: Web Service
- **Name**: `smart-campus-frontend`
- **Runtime**: `Docker`
- **Root Directory**: `frontend`
- **Port**: `80`
- **Environment Variables**:
  - `BACKEND_URL`: *(Internal Render host URL provided in the backend dashboard under Host, e.g. `smart-campus-backend:10000`)*
  - `NGINX_ENVSUBST_FILTER`: `BACKEND_URL`

---

## 5. Domain Name & HTTPS Configuration
- Render automatically provisions free, fully managed Let's Encrypt TLS certificates (HTTPS) for both the frontend and backend public endpoints.
- Custom domains can be linked to the frontend by adding a CNAME record mapping to your `xxx.onrender.com` address.

---

## 6. Troubleshooting & Diagnostics
- **Document Conversion Times out**: Ensure `proxy_read_timeout` in Nginx is set to `300s` to support larger file conversions.
- **LibreOffice Path Errors**: Verify `SMARTTOOLS_LIBREOFFICE_PATH` is set to `/usr/bin/soffice` and the backend Docker image successfully executes `soffice --version`.
- **Database SSL Failures**: If your external provider requires SSL connection, set `DB_SSL` to `true` in the environment variables to enforce encrypted transport.
