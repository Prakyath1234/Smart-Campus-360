# Smart Campus 360 - REST API Contract Specifications

## 1. Authentication & Onboarding
- `POST /api/auth/login`: Authenticate user and return JWT bearer token.
- `POST /api/auth/register`: Onboard new user (Student / Faculty / Admin / Security).
- `GET /api/auth/departments`: Fetch public list of academic departments.

---

## 2. Admin Management Core (ROLE_ADMIN)
- `POST /api/admin/users`: Create a new user account with BCrypt password hashing.
- `GET /api/admin/users`: Search, filter (role, status), and paginate user accounts.
- `GET /api/admin/users/{id}`: Fetch user by ID (passwords never exposed).
- `PUT /api/admin/users/{id}`: Update user profile and roles.
- `PATCH /api/admin/users/{id}/status`: Toggle user `enabled` state (soft deactivation).
- `DELETE /api/admin/users/{id}`: Physically remove user account.

- `POST /api/admin/students`: Create student profile and link to user account.
- `GET /api/admin/students`: Search students by name, email, roll number, department, semester, and status.
- `GET /api/admin/students/{id}`: Fetch student details by ID.
- `PUT /api/admin/students/{id}`: Update student roll number, department, or semester.
- `PATCH /api/admin/students/{id}/status`: Enable/disable student account.

- `POST /api/admin/faculties`: Create faculty instructor profile.
- `GET /api/admin/faculties`: Search and paginate faculty profiles.
- `GET /api/admin/faculties/{id}`: Fetch faculty details by ID.
- `PUT /api/admin/faculties/{id}`: Update employee ID, designation, or department.
- `DELETE /api/admin/faculties/{id}`: Remove faculty profile.

- `POST /api/admin/departments`: Create department.
- `GET /api/admin/departments`: List all departments.
- `POST /api/admin/subjects`: Register new course subject.
- `GET /api/admin/subjects`: List all registered subjects.
- `POST /api/admin/timetable`: Create conflict-checked timetable slot.

---

## 3. Student Performance Analytics & Risk Engine
- `GET /api/student/performance`: Calculate attendance rate, average score, top/weakest subject, and `AT_RISK` flag (<75% attendance or <50% average) for the logged-in student.
- `GET /api/admin/analytics/at-risk`: List all students flagged as `AT_RISK` across campus.
- `GET /api/faculty/mentees/{studentId}/performance`: View performance metrics for assigned mentee.

---

## 4. Faculty Mentorship & Counseling Notes
- `POST /api/admin/mentorship/assign`: Assign faculty mentor to a student mentee.
- `DELETE /api/admin/mentorship/{menteeId}`: Unassign mentor.
- `GET /api/faculty/mentees`: Retrieve list of assigned mentees for faculty.
- `POST /api/faculty/mentor-notes`: Log private faculty counseling note (`ACADEMIC`, `PERSONAL`, `DISCIPLINARY`, `CAREER`).
- `GET /api/faculty/mentees/{studentId}/notes`: Fetch counseling history for student.
- `PUT /api/faculty/mentor-notes/{id}`: Edit counseling note.
- `DELETE /api/faculty/mentor-notes/{id}`: Delete note.

---

## 5. Campus Service Requests Module
- `POST /api/student/service-requests`: Submit service ticket (`ID_CARD`, `BONAFIDE`, `HOSTEL_MAINTENANCE`, `TRANSPORT`, `LIBRARY`, `FEE_RECEIPT`, `DOCUMENT`, `OTHER`).
- `GET /api/student/service-requests`: List logged-in student's service tickets.
- `GET /api/admin/service-requests`: Paginate and filter all campus service tickets.
- `PUT /api/admin/service-requests/{id}/status`: Admin update ticket status (`PENDING`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REJECTED`) with notes.

---

## 6. Emergency Telemetry & Response Analytics
- `POST /api/emergency/trigger`: Dispatch emergency distress alert with optional GPS lat/long.
- `GET /api/emergency/alerts`: Fetch all emergency incidents.
- `GET /api/emergency/active`: Fetch currently active emergency incidents.
- `GET /api/emergency/analytics`: Fetch emergency metrics (total incidents, type distribution, resolved count, avg response time).
- `PUT /api/emergency/{id}/status`: Update incident state (`ACKNOWLEDGED`, `IN_PROGRESS`, `RESOLVED`, `CANCELLED`).

---

## 7. Targeted Announcements System
- `POST /api/announcements`: Publish broadcast announcement with target audience (`ALL_STUDENTS`, `DEPARTMENT`, `SEMESTER`, `FACULTY`).
- `GET /api/announcements`: Get published announcements targeted to current user.
- `GET /api/announcements/all`: Admin/faculty view all announcements.
- `DELETE /api/announcements/{id}`: Delete announcement.

---

## 8. CSV Reports Engine
- `GET /api/reports/csv?type=students`: Download CSV of student roster.
- `GET /api/reports/csv?type=attendance`: Download CSV of attendance logs.
- `GET /api/reports/csv?type=marks`: Download CSV of academic marks.
- `GET /api/reports/csv?type=complaints`: Download CSV of grievance tickets.
- `GET /api/reports/csv?type=leaves`: Download CSV of leave applications.
- `GET /api/reports/csv?type=emergency`: Download CSV of emergency telemetry history.
- `GET /api/reports/csv?type=service-requests`: Download CSV of service tickets.

---

## 9. Academic Calculator
- `POST /api/academic/calculator/sgpa`: Calculate SGPA from courses list (credits, grade points).
- `POST /api/academic/calculator/cgpa`: Calculate CGPA from semester SGPA records.
- `POST /api/academic/calculator/what-if`: Simulate semester grade projections.
- `POST /api/academic/calculator/target-cgpa`: Required future SGPA estimator for target CGPA parameters.
- `GET /api/student/academic/summary`: Retrieve real student authenticated history and CGPA tracking.

---

## 10. ATS Resume Analyzer
- `POST /api/ats/analyze`: Matches uploaded resume file and raw job description text to return compatibility scores and suggestions (requires authentication).
