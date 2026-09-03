# Render Cloud Production Deployment Guide (Vercel Frontend + Render Backend + External Aiven MySQL)

This guide describes how the SmartCampus 360 suite is deployed to cloud production, using **Vercel** for the React frontend, **Render** for the Spring Boot backend, and **Aiven Cloud** for the managed MySQL 8.0 database.

---

## 🌐 Live Production Deployments

* **Production Frontend (Vercel)**: [https://smart-campus-360-sandy.vercel.app](https://smart-campus-360-sandy.vercel.app)
* **Production Backend (Render)**: [https://smart-campus-backend-8v6m.onrender.com](https://smart-campus-backend-8v6m.onrender.com)
* **Health Check Endpoint**: [https://smart-campus-backend-8v6m.onrender.com/actuator/health](https://smart-campus-backend-8v6m.onrender.com/actuator/health)
* **Database Provider**: Managed Aiven Cloud MySQL 8.0 (`DB_SSL=true`)

---

## 1. Infrastructure Overview

```
       [ Client Browser ]
               │
               ├─────────────────────────────────────────────┐
               ▼ (Public HTTPS Web Traffic)                  ▼ (API Requests / CORS)
  [ Vercel Frontend CDN ]                      [ Render Backend Web Service ]
   └─ React 18 SPA                              └─ Java 21 + LibreOffice
   └─ URL: https://smart-campus-360-sandy.vercel.app └─ URL: https://smart-campus-backend-8v6m.onrender.com
                                                             │
                                                             ▼ (Remote SSL Connection)
                                                 [ Aiven Cloud Managed MySQL ]
                                                  └─ Port: 27994 (SSL Required)
```

---

## 2. Environment Variables Configuration

### A. Render Backend Environment Variables
* `SPRING_PROFILES_ACTIVE`: `mysql`
* `DB_HOST`: `mysql-419b4a3-manojkumar829638-354d.c.aivencloud.com`
* `DB_PORT`: `27994`
* `DB_NAME`: `defaultdb`
* `DB_USERNAME`: `avnadmin`
* `DB_PASSWORD`: `******` *(Configured via Render Environment Secrets)*
* `DB_SSL`: `true`
* `JWT_SECRET`: `******` *(Render Generated Secret)*
* `SMARTTOOLS_LIBREOFFICE_PATH`: `/usr/bin/soffice`
* `CORS_ALLOWED_ORIGINS`: `https://smart-campus-360-sandy.vercel.app,https://*.vercel.app`

### B. Vercel Frontend Environment Variables
* `VITE_API_URL`: `https://smart-campus-backend-8v6m.onrender.com`

---

## 3. SPA Routing & Vercel Configuration ([`frontend/vercel.json`](file:///f:/360/Smart-Campus-360/frontend/vercel.json))

```json
{
  "rewrites": [
    {
      "source": "/api/(.*)",
      "destination": "https://smart-campus-backend-8v6m.onrender.com/api/$1"
    },
    {
      "source": "/(.*)",
      "destination": "/index.html"
    }
  ]
}
```
