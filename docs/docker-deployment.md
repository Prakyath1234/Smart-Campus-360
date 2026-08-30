# Docker Production Deployment Guide

This guide details the containerized architecture, environment parameters, database structures, and operations of the SmartCampus 360 suite.

---

## 1. Architecture Overview
The application runs as a multi-container environment orchestrated via `docker compose`:

```
          [ Browser / Client ]
                   ↓ (Port 80)
         [ Nginx Web Server (Frontend) ]
          /                         \
  (Static React App)         (API Proxy to Port 8080)
        ↓                              ↓
[ static assets ]               [ Spring Boot API (Backend) ]
                                /              \
                       [ MySQL DB ]     [ Headless LibreOffice ]
```

- **Frontend Container**: Serving compiled React assets via Nginx. Proxy mappings route `/api/*` requests internally to the backend.
- **Backend Container**: Executing the Spring Boot JAR with headless LibreOffice capabilities.
- **MySQL Container**: Storing persistent relational database schemas.

---

## 2. Docker Setup Configuration

### Requirements
- Docker Engine >= 20.10
- Docker Compose >= 2.0

### Database Persistence
MySQL database files are persisted using a named Docker volume (`mysql_data`) mapping to `/var/lib/mysql` inside the database container. This ensures data survives `docker compose down`.

---

## 3. Environment Variables Configuration

The following parameters configure Spring Boot and database connections:

| Environment Variable | Default Value | Description |
|---|---|---|
| `DB_HOST` | `mysql` | Compose service hostname of the database server |
| `DB_PORT` | `3306` | Connection port |
| `DB_NAME` | `smart_campus_360` | Targeting database schema name |
| `DB_USERNAME` | `smart_user` | Non-root access credentials |
| `DB_PASSWORD` | `smart_password` | Database user password |
| `SPRING_PROFILES_ACTIVE` | `mysql` | Activates MySQL JPA configurations |
| `SMARTTOOLS_LIBREOFFICE_PATH` | `/usr/bin/soffice` | HEADLESS converter program location |
| `JWT_SECRET` | *(Random 256-bit Hex)* | Secret used to sign JWT auth tokens |

---

## 4. LibreOffice Integration & Concurrency
- The backend container JRE includes the official modular LibreOffice packages (`libreoffice-writer`, `calc`, `impress`, and `common`).
- Concurrent conversion processes are isolated via a platform-independent parameter flag (`-env:UserInstallation=file:///tmp/.../profile`). This guarantees that multi-user calls do not lock or corrupt the default profile directory.
- Temporary files and directories are recursively deleted upon completion or timeout.

---

## 5. Startup & Shutdown Operations

### One-Command Startup
To build and start all services:
```bash
docker compose up --build
```

### Detached Mode Startup
```bash
docker compose up -d --build
```

### Shutdown
To stop all running containers safely (preserving database data):
```bash
docker compose down
```

---

## 6. Logs & Troubleshooting

### Viewing Service Status
```bash
docker compose ps
```

### Inspecting Container Logs
* **Backend logs**:
  ```bash
  docker compose logs backend
  ```
* **Frontend/Nginx logs**:
  ```bash
  docker compose logs frontend
  ```
* **Database logs**:
  ```bash
  docker compose logs mysql
  ```

### Restarting Services
```bash
docker compose restart backend
```
