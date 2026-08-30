# Render Cloud Production Deployment Guide

This guide describes how to deploy the SmartCampus 360 suite to Render using the Blueprint infrastructure specification.

---

## 1. Prerequisites
- A verified [Render.com](https://render.com) account.
- The codebase pushed to a private or public GitHub repository.

---

## 2. Infrastructure Overview
Render orchestrates the application using three distinct services connected via a private bridged network:

```
        [ Client Browser ]
                ↓ (Public URL)
   [ Web Service: smart-campus-frontend ]
    └─ Env: Nginx + Compiled React SPA
    └─ Port: 80
                ↓ (Proxy Pass via BACKEND_URL)
    [ Web Service: smart-campus-backend ]
     └─ Env: Java 21 + LibreOffice
     └─ Port: $PORT (Dynamic)
     └─ Health Check: /actuator/health
                ↓ (Internal DNS)
   [ Private Service: smart-campus-mysql ]
    └─ Env: MySQL 8.0
    └─ Volume Disk: mysql-data (/var/lib/mysql)
    └─ Port: 3306 (Not publicly exposed)
```

---

## 3. Deploying using Render Blueprints (`render.yaml`)

1. Navigate to the Render Dashboard.
2. Click **New** -> **Blueprint**.
3. Connect your GitHub repository containing the codebase.
4. Render will auto-detect the [`render.yaml`](file:///f:/360/Smart-Campus-360/render.yaml) configuration.
5. Review the plan details and click **Apply**.

---

## 4. Manual Deployment Setup (Alternative)

If you prefer to configure the services manually via the Render GUI, use the following specifications:

### A. MySQL (Private Service)
- **Service Type**: Private Service
- **Name**: `smart-campus-mysql`
- **Root Directory**: `.` (or use Image `mysql:8.0`)
- **Docker Image**: `mysql:8.0`
- **Disk**: Mount a persistent disk at `/var/lib/mysql` (Size: >= 5 GB).
- **Environment Variables**:
  - `MYSQL_DATABASE`: `smart_campus_360`
  - `MYSQL_USER`: `smart_user`
  - `MYSQL_PASSWORD`: *(Generate secure password)*
  - `MYSQL_ROOT_PASSWORD`: *(Generate secure root password)*

### B. Backend (Web Service)
- **Service Type**: Web Service
- **Name**: `smart-campus-backend`
- **Runtime**: `Docker`
- **Root Directory**: `backend`
- **Docker Command**: Runs automatically from Dockerfile ENTRYPOINT.
- **Port**: Bind to Render's default dynamic `$PORT`.
- **Health Check Path**: `/actuator/health`
- **Environment Variables**:
  - `SPRING_PROFILES_ACTIVE`: `mysql`
  - `DB_HOST`: `smart-campus-mysql` (Or internal service host URL provided by Render)
  - `DB_PORT`: `3306`
  - `DB_NAME`: `smart_campus_360`
  - `DB_USERNAME`: `smart_user`
  - `DB_PASSWORD`: *(Matching MYSQL_PASSWORD)*
  - `JWT_SECRET`: *(Generate secure 256-bit Hex string)*
  - `SMARTTOOLS_LIBREOFFICE_PATH`: `/usr/bin/soffice`

### C. Frontend (Web Service)
- **Service Type**: Web Service
- **Name**: `smart-campus-frontend`
- **Runtime**: `Docker`
- **Root Directory**: `frontend`
- **Port**: `80`
- **Environment Variables**:
  - `BACKEND_URL`: `http://smart-campus-backend:8080` (Internal Render host resolution URL)
  - `NGINX_ENVSUBST_FILTER`: `BACKEND_URL`

---

## 5. Domain Name & HTTPS Configuration
- Render automatically provisions free, fully managed Let's Encrypt TLS certificates (HTTPS) for both the frontend and backend public endpoints.
- Custom domains can be linked to the frontend by adding a CNAME record mapping to your `xxx.onrender.com` address.

---

## 6. Troubleshooting & Diagnostics
- **Document Conversion Times out**: Ensure `proxy_read_timeout` in Nginx is set to `300s` to support larger file conversions.
- **LibreOffice Path Errors**: Verify `SMARTTOOLS_LIBREOFFICE_PATH` is set to `/usr/bin/soffice` and the backend Docker image successfully executes `soffice --version`.
- **Database Connection Refused**: Verify that `smart-campus-mysql` has finished initializing before backend tries to connect. The backend uses Compose/Render status check dependency mappings to prevent startup failures.
