# Smart Campus 360 - Enterprise Campus Management & Safety Telemetry Platform

**Smart Campus 360** is an enterprise-grade campus management and emergency response telemetry platform built with **Spring Boot 3.3.x (Java 21)** and **React 18** (Glassmorphic Dark UI + Framer Motion).

---

## 🌟 Key Platform Features

### 🎓 1. Academic & Student Management
- **Student Workspace**: Profile details, real-time attendance rate calculation, subject-wise internal/exam mark breakdown, and class schedules.
- **Student Performance Analytics & Risk Engine**: Evaluates attendance (<75%) and academic scores (<50 avg) to flag `AT_RISK` students with explicit reasons.
- **Faculty Mentorship & Counseling**: Faculty-mentee mapping with private counseling notes (`ACADEMIC`, `PERSONAL`, `DISCIPLINARY`, `CAREER`).

### 🏛️ 2. Administrative Operations & Full CRUD
- **User & Role Management**: Search, filter (role, status), and paginate user accounts with soft deactivation (`enabled = false`).
- **Student & Faculty Management**: Roll number and employee ID tracking with department/semester filters.
- **Department & Subject Registry**: Conflict-checked timetable scheduler preventing classroom and semester slot collisions.
- **Campus Service Requests**: Ticket submission and processing for ID cards, bonafide certificates, hostel maintenance, and fee receipts.

### 🛡️ 3. Emergency SOS Response & Security Center
- **SOS Telemetry Feed**: Live incident feed with emergency types (`MEDICAL`, `FIRE`, `SECURITY`, `ACCIDENT`).
- **GPS Coordinates & Google Maps Link**: Transmits HTML5 GPS latitude/longitude with fallback location text if permission is denied.
- **Enforced State Machine Workflow**: Strict status transition validation (`ACTIVE` → `ACKNOWLEDGED` → `IN_PROGRESS` → `RESOLVED` / `CANCELLED`).
- **Telemetry Analytics**: Response time calculations, type distribution, and resolved count metrics.

### 📜 4. Auditability, Status History & Reports
- **Append-Only Audit Logs**: Immutable log records (`USER_CREATED`, `MARKS_UPDATED`, `LEAVE_APPROVED`, `SOS_TRIGGERED`, `SOS_RESOLVED`) queryable by Admin (`GET /api/admin/audit-logs`).
- **Status History Tracking**: Audit trails for status changes across Complaints, Leaves, Service Requests, and Emergency Alerts.
- **CSV Reporting Engine**: Export downloadable CSV reports for students, attendance, marks, complaints, leaves, emergencies, and service requests.

---

## 🛠️ Technology Stack

- **Backend Framework**: Java 21, Spring Boot 3.3.3, Spring Security, JWT (Stateless Bearer Tokens), BCrypt.
- **ORM & Data Access**: Spring Data JPA, Hibernate ORM 6.5, Jakarta Validation.
- **Database Support**: H2 (In-memory development profile) & MySQL 8.0 (Persistent production profile).
- **Frontend Framework**: React 18, Vite 8.2, Framer Motion, Lucide Icons, Vanilla CSS (Glassmorphism aesthetics).
- **Testing & QA**: JUnit 5, Spring Boot Test, Mockito.

---

## ⚡ Quick Start Instructions

### 1. Run Backend (H2 Profile)
```powershell
cd backend
C:\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=h2"
```

### 2. Run Frontend Dev Server
```powershell
cd frontend
npm run dev
```
Open **`http://localhost:5173`** in your browser.

### 3. Run Automated Test Suite
```powershell
cd backend
C:\tools\apache-maven-3.9.9\bin\mvn.cmd clean test
```

---

## 🐳 Containerization & Production Deployment

### Local Docker Deployment
Run the complete containerized environment locally using one command:
```powershell
docker compose up --build
```
This runs the frontend Nginx (port `80`), Spring Boot backend (port `8080`), and MySQL 8.0 server with isolated LibreOffice conversion pipelines.

### Render Cloud Deployment
For cloud production setup using Render, refer to the Blueprint configuration:
- Blueprint schema: [`render.yaml`](file:///f:/360/Smart-Campus-360/render.yaml)
- Deployment instructions: [docs/render-deployment.md](file:///f:/360/Smart-Campus-360/docs/render-deployment.md)

---

## 📚 Technical Documentation Suite

- [docs/architecture-review.md](file:///f:/360/Smart-Campus-360/docs/architecture-review.md): System architecture, layer decomposition, and access control matrix.
- [docs/api-contract.md](file:///f:/360/Smart-Campus-360/docs/api-contract.md): Complete REST API contract specifications.
- [docs/crud-guide.md](file:///f:/360/Smart-Campus-360/docs/crud-guide.md): Detailed breakdown of CRUD layers and validation.
- [docs/security-review.md](file:///f:/360/Smart-Campus-360/docs/security-review.md): Security hardening, JWT authentication, and IDOR prevention controls.
- [docs/audit-log.md](file:///f:/360/Smart-Campus-360/docs/audit-log.md): Append-only audit logging system reference.
- [docs/testing-strategy.md](file:///f:/360/Smart-Campus-360/docs/testing-strategy.md): Automated testing design and coverage details.
- [docs/interview-notes.md](file:///f:/360/Smart-Campus-360/docs/interview-notes.md): Answer guide to the 15 technical interview prep questions.
- [docs/final-verification-report.md](file:///f:/360/Smart-Campus-360/docs/final-verification-report.md): Summary report of final verification across all 20 phases.
- [docs/docker-deployment.md](file:///f:/360/Smart-Campus-360/docs/docker-deployment.md): Docker Compose architecture and parameters.
- [docs/render-deployment.md](file:///f:/360/Smart-Campus-360/docs/render-deployment.md): Render deployment and services mapping.
