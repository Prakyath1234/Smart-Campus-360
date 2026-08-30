# Smart Campus 360 - REST API Documentation

All endpoints return JSON responses and are mapped relative to the server context root (default: `http://localhost:8080`).

---

## 1. Authentication Services
Endpoints grouped under `/api/auth` do not require JWT authorization headers (public access).

### User Registration
* **URL:** `/api/auth/register`
* **Method:** `POST`
* **Request Body:**
  ```json
  {
    "name": "John Doe",
    "email": "student@sode-edu.in",
    "password": "password123",
    "phone": "9876543212",
    "role": "STUDENT",
    "rollNumber": "CS2026001",
    "departmentId": 1,
    "semester": 5
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "token": "eyJhbGciOi...",
    "email": "student@sode-edu.in",
    "role": "STUDENT",
    "name": "John Doe",
    "userId": 3,
    "profileId": 1
  }
  ```

### User Login
* **URL:** `/api/auth/login`
* **Method:** `POST`
* **Request Body:**
  ```json
  {
    "email": "student@sode-edu.in",
    "password": "password"
  }
  ```
* **Response (200 OK):** Mapped identically to Registration response.

### Fetch Catalog Departments
* **URL:** `/api/auth/departments`
* **Method:** `GET`
* **Response (200 OK):** Lists all departments.

---

## 2. Emergency SOS Services
Protected endpoints requiring a valid `Bearer <JWT>` token.

### Trigger SOS Signal
* **URL:** `/api/emergency/sos`
* **Method:** `POST`
* **Authorized Roles:** `STUDENT`
* **Request Body:**
  ```json
  {
    "studentId": 1,
    "emergencyType": "MEDICAL",
    "description": "Passed out in CSE Lab 1",
    "latitude": 12.971598,
    "longitude": 77.594562,
    "locationText": "CSE Dept, Floor 3"
  }
  ```
* **Response (200 OK):** Echoes saved alert log details.

### Fetch Active Emergencies
* **URL:** `/api/emergency/active`
* **Method:** `GET`
* **Authorized Roles:** `SECURITY`, `ADMIN`

### Acknowledge SOS
* **URL:** `/api/emergency/{id}/acknowledge`
* **Method:** `PUT`
* **Authorized Roles:** `SECURITY`, `ADMIN`

---

## 3. Academic & Management Services
Protected endpoints requiring a valid `Bearer <JWT>` token.

### Submit Daily Student Attendance
* **URL:** `/api/faculty/attendance`
* **Method:** `POST`
* **Authorized Roles:** `FACULTY`
* **Request Body:**
  ```json
  {
    "studentId": 1,
    "subjectId": 2,
    "date": "2026-08-26",
    "status": "PRESENT"
  }
  ```

### Submit Academic Grades
* **URL:** `/api/faculty/marks`
* **Method:** `POST`
* **Authorized Roles:** `FACULTY`
* **Request Body:**
  ```json
  {
    "studentId": 1,
    "subjectId": 1,
    "internalMarks": 18.5,
    "assignmentMarks": 9.0,
    "examMarks": 65.0
  }
  ```

### Apply Student Leave Request
* **URL:** `/api/students/leave`
* **Method:** `POST`
* **Authorized Roles:** `STUDENT`

### Log a Student Complaint (AI Classified)
* **URL:** `/api/students/complaint`
* **Method:** `POST`
* **Authorized Roles:** `STUDENT`
* **Request Body:**
  ```json
  {
    "studentId": 1,
    "title": "Corridor wires exposed",
    "description": "There is a bare copper cable hanging from server ceiling. Shock hazard."
  }
  ```
* **AI Classifier Output (Categories auto-set by backend):**
  - Category: `SAFETY`
  - Priority: `CRITICAL`
