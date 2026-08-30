# Smart Campus 360 - Comprehensive Setup & Run Guide

**Smart Campus 360** is an integrated college management and emergency response system. It unifies academic management (attendance, grades, timetables, leave requests, complaint ticket resolution) with a real-time Distress Signal (SOS) emergency dispatch system.

---

## 🛠️ Prerequisites & Prerequisites Setup

- **Java Development Kit (JDK)**: Java 21 or higher installed.
- **Apache Maven**: Version 3.9.9 (Installed at `C:\tools\apache-maven-3.9.9` and present on System/User `PATH`).
- **Database Options**:
  - **Option 1 (Default / In-Memory)**: **H2 Database** — Zero setup required.
  - **Option 2 (Persistent)**: **MySQL 8.0 / 9.1** — Requires running MySQL server.

---

## 🚀 How to Run the Project (Step-by-Step)

### Option 1: Quick Run with In-Memory H2 Database (Recommended)

1. **Open PowerShell / Command Prompt** in the project directory:
   ```powershell
   cd f:\360\Smart-Campus-360\backend
   ```

2. **Run the Spring Boot application using the `h2` active profile**:
   - **Recommended Windows Command (Works in current terminal)**:
     ```powershell
     C:\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=h2"
     ```
   - **Or refresh PATH in your current session first**:
     ```powershell
     $env:Path += ";C:\tools\apache-maven-3.9.9\bin"
     mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=h2"
     ```

3. **Wait for startup log**:
   Look for the log confirmation:
   ```text
   INFO ... Tomcat started on port 8080 (http) with context path '/'
   INFO ... Started SmartCampusApplication in X seconds
   ```

4. **Access the Portal**:
   Open your browser and navigate to:
   - **Home / Landing Page**: [http://localhost:8080](http://localhost:8080)
   - **Login Page**: [http://localhost:8080/login.html](http://localhost:8080/login.html)
   - **H2 DB Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     - *JDBC URL*: `jdbc:h2:mem:smartcampusdb`
     - *User*: `sa`
     - *Password*: *(blank)*

---

### Option 2: Run with MySQL Database

1. **Import Database Schema & Data**:
   Open PowerShell and execute:
   ```powershell
   cd f:\360\Smart-Campus-360
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/sample-data.sql
   ```

2. **Configure Database Credentials (Optional)**:
   If your MySQL password is not empty, set environment variables before running:
   ```powershell
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_mysql_password"
   ```

3. **Start the Backend Application**:
   ```powershell
   cd f:\360\Smart-Campus-360\backend
   C:\tools\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
   ```

4. **Access the Application**:
   Open browser at [http://localhost:8080](http://localhost:8080).

---

## 🔐 Default Demo Accounts

All pre-configured accounts use the password: **`password`**

| Role | Email | Password | Features & Permissions |
|---|---|---|---|
| 👑 **ADMIN** | `admin@sode-edu.in` | `password` | System stats, registration, assigning subjects, managing departments & timetables, complaint resolution |
| 👨‍🏫 **FACULTY** | `faculty@sode-edu.in` | `password` | Attendance entry, internal/exam grade recording, reviewing student leave requests |
| 🎓 **STUDENT** | `student@sode-edu.in` | `password` | Viewing marks/attendance, submitting complaints, requesting leave, triggering instant SOS alerts |
| 🛡️ **SECURITY** | `security@sode-edu.in` | `password` | Real-time distress telemetry feed, live GPS location tracking of alerts, status toggle |

---

## 🌐 Application URLs & Pages

- **Landing Page**: `http://localhost:8080/index.html`
- **Login Page**: `http://localhost:8080/login.html`
- **Register Page**: `http://localhost:8080/register.html`
- **Student Dashboard**: `http://localhost:8080/student/dashboard.html`
- **Faculty Dashboard**: `http://localhost:8080/faculty/dashboard.html`
- **Admin Dashboard**: `http://localhost:8080/admin/dashboard.html`
- **Security Dashboard**: `http://localhost:8080/security/dashboard.html`

---

## 🧪 Building & Running Unit Tests

To run all automated JUnit tests against the test suite:
```powershell
cd f:\360\Smart-Campus-360\backend
C:\tools\apache-maven-3.9.9\bin\mvn.cmd test "-Dspring.profiles.active=h2"
```

---

## 📌 Troubleshooting Tips

- **Port 8080 Already in Use**:
  If port 8080 is occupied, you can change the port when running:
  ```powershell
  mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081 --spring.profiles.active=h2"
  ```
- **Maven Not Found**:
  Make sure `C:\tools\apache-maven-3.9.9\bin` is included in your system/user `PATH` environment variable.
