# Smart Campus 360

Smart Campus Management &amp; Emergency Response System

---

## Description
**Smart Campus 360** is a college management and emergency response system. It provides students, faculty members, administrators, and security staff with a unified workspace, integrating academic logs (grades, scheduling, attendance) with real-time distress signals (SOS emergency alerts).

## Problem Statement
Traditional campuses operate with fragmented systems. Academic grades, schedules, and attendance logs are separated from critical facility safety response mechanisms. In critical situations (e.g. fire hazards, medical collapses), student coordinate reporting is slow, causing delays in response.

## Objectives
1. **Unify Operations:** Consolidate class schedules, marks, attendance logs, and leave requests into one dashboard.
2. **Swift Safety Response:** Implement a high-priority SOS emergency alert system utilizing browser geolocations to notify security staff instantly.
3. **AI Integration:** Leverage automated keyword NLP classification to route complaints to appropriate categories and priority channels.

## User Roles
* **STUDENT:** View attendance, marks, schedules. Submit leaves, complaints, and trigger SOS.
* **FACULTY:** Record attendance logs, update grades, and review leave requests.
* **ADMIN:** Complete dashboard stats, register departments, assign subjects, add timetables, and resolve complaints.
* **SECURITY:** Live SOS telemetry feed with student contact details and toggle response workflow controls.

## Technology Stack
* **Backend:** Java 21, Spring Boot 3.3.x, Spring Data JPA, Spring Security, JWT (HMAC-SHA), Lombok, Jakarta Validation.
* **Database:** MySQL 8.0, H2 Database (in-memory test fallback).
* **Frontend:** Responsive HTML5, CSS3, JavaScript (Fetch API, local storage), Bootstrap 5.
* **Testing:** JUnit 5, Mockito.

---

## Architecture
The application implements a clean layered architecture pattern:
```
Frontend (Bootstrap 5 / Vanilla JS Fetch)
       ↓
REST Controller Layer
       ↓
Service Layer (Business Logic & AI Classifier)
       ↓
Repository Layer (Spring Data JPA)
       ↓
Database Layer (H2 / MySQL)
```

---

## Installation & Setup

### 1. MySQL Setup
Import the database scripts provided in the `database/` folder:
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample-data.sql
```

### 2. Backend Setup
Set environment variables if customizing credentials:
- `DB_USERNAME` (Default: `root`)
- `DB_PASSWORD` (Default: empty)
- `JWT_SECRET` (Default: secure fallback key)

Compile and test:
```bash
cd backend
mvn clean test
```

Start the server:
```bash
mvn spring-boot:run
```
For fallback zero-config H2 setup, start using:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=h2"
```

### 3. Frontend Setup
1. Launch the backend server (`http://localhost:8080`).
2. Double click or serve `frontend/index.html` in any browser.

---

## Future Enhancements
* **WebSockets Integration:** Transition from polling to live WebSockets for instant, low-latency SOS alarm dispatching.
* **Google Maps Maps API:** Embed interactive location pins rather than linking to external Google Maps navigation lines.
* **Third-Party Email Dispatch:** Integrate SendGrid/Amazon SES to issue copy leave logs and grade report cards.
