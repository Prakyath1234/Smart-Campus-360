# Smart Campus 360 - Technical Interview Prep & Core Concepts

This guide contains detailed answers to the 15 technical interview questions covering backend design, database ORM, security, performance calculation, emergency handling, and full-stack integration.

---

### Q1. Explain CRUD in your project.
**Answer**:
CRUD stands for Create, Read, Update, and Delete. In Smart Campus 360, full production-quality CRUD is implemented for core modules: Users, Students, Faculty, Departments, and Subjects.
- **Create**: REST `POST` endpoints accepting JSON DTOs validated with Bean Validation (`@Valid`) and encrypted passwords.
- **Read**: REST `GET` endpoints returning DTOs with dynamic search, filtering, and Spring Data `Pageable` pagination.
- **Update**: REST `PUT` and `PATCH` endpoints allowing partial or full record updates.
- **Delete / Deactivate**: `DELETE` endpoints for physical removal and `PATCH /status` for soft deactivation (`enabled = false`), preserving historical attendance, grade, and leave audit trails.

---

### Q2. Why use DTOs (Data Transfer Objects)?
**Answer**:
1. **Security**: Prevents returning sensitive fields like password hashes to the client.
2. **Decoupling**: Decouples internal database schemas from external API contracts.
3. **Prevent Infinite Recursion**: Prevents infinite JSON serialization loops caused by bi-directional JPA relationships (`@ManyToOne` / `@OneToMany`).
4. **Prevent `LazyInitializationException`**: Controls exactly which fields are serialized before closing transactional sessions.

---

### Q3. Why use a Service layer?
**Answer**:
The Service layer isolates business logic from HTTP transport concerns in Controllers. It manages database transactions (`@Transactional`), coordinates multiple repositories, enforces authorization checks, and performs calculations (such as timetable conflict detection or grade computation).

---

### Q4. Why use a Repository layer?
**Answer**:
The Repository layer abstracts data access. By extending `JpaRepository` and `JpaSpecificationExecutor`, Spring Data JPA automatically generates implementation classes at runtime for standard SQL queries, dynamic criteria searching, and pagination without writing boilerplate JDBC code.

---

### Q5. How does JPA work?
**Answer**:
JPA (Java Persistence API) is a standard ORM specification that maps Java classes (Entities) to relational database tables. Annotations like `@Entity`, `@Table`, `@Id`, and `@Column` define table schemas.

---

### Q6. How does Hibernate map entities?
**Answer**:
Hibernate is the ORM provider implementing JPA. It translates Java object operations (like `repository.save(student)`) into native SQL queries (`INSERT`, `UPDATE`, `SELECT`, `DELETE`), manages persistence contexts, and maintains first-level entity caching.

---

### Q7. How do you prevent duplicate data?
**Answer**:
1. **Database Constraints**: Table columns use unique constraints (`@Column(unique = true)` on `email`, `rollNumber`, `employeeId`, `code`).
2. **Service Layer Validation**: Service methods invoke `repository.existsByEmail(...)` or `existsByRollNumber(...)` and throw a `BadRequestException` (`HTTP 400`) before saving duplicate records.

---

### Q8. How does pagination work?
**Answer**:
Using Spring Data `Pageable` and `PageRequest.of(page, size, Sort)`. Repositories execute a SQL `COUNT` query followed by a paginated `SELECT` query using `LIMIT` and `OFFSET` (or `FETCH FIRST / OFFSET` in H2).

---

### Q9. How does JWT authentication work?
**Answer**:
1. User logs in at `/api/auth/login` with email and password.
2. Server verifies password hash with `BCryptPasswordEncoder` and returns a signed JWT token containing email and role claims.
3. Client stores JWT in `localStorage` and includes header `Authorization: Bearer <token>` on requests.
4. `JwtAuthenticationFilter` intercepts requests, validates the signature, extracts the user's email, and sets `SecurityContextHolder.getContext().setAuthentication(...)`.

---

### Q10. How do you prevent one student accessing another student's data (IDOR protection)?
**Answer**:
Instead of accepting arbitrary student IDs in path variables for personal data endpoints (e.g. `/api/student/profile`), endpoints derive identity dynamically from the authenticated JWT `Principal` (`principal.getName()`). This makes IDOR attacks impossible because students can only fetch records linked to their authenticated identity.

---

### Q11. How does timetable conflict detection work?
**Answer**:
Before saving a new timetable slot, `AcademicService` checks:
1. **Classroom Overlap**: `timetableRepository.findByDayOfWeekAndClassroom(...)` ensures no two classes occupy the same room at overlapping times.
2. **Semester Conflict**: `timetableRepository.findByDayOfWeekAndSemesterAndDepartmentId(...)` ensures students in the same semester/department do not have overlapping lectures.

---

### Q12. How is student performance calculated?
**Answer**:
`PerformanceAnalyticsService` computes:
- **Attendance Rate**: `(presentClasses / totalClasses) * 100.0`
- **Average Marks**: Average of total subject scores (internal + assignment + exam)
- **Risk Status Engine**: Evaluates configurable rules. If `attendancePercentage < 75%` OR `averageMarks < 50`, the student is flagged as `AT_RISK` with an explicit reason string; otherwise `GOOD`.

---

### Q13. How does SOS work?
**Answer**:
1. Student clicks "TRIGGER SOS" on web dashboard.
2. Browser fetches optional HTML5 Geolocation coordinates (`latitude`, `longitude`).
3. Request posts to `/api/emergency/trigger`.
4. Server creates `EmergencyAlert` (status `ACTIVE`), sends instant notifications to all Security & Admin users, and records telemetry.

---

### Q14. How are exceptions handled?
**Answer**:
`GlobalExceptionHandler` (`@RestControllerAdvice`) intercepts all runtime exceptions (`ResourceNotFoundException`, `BadRequestException`, `AccessDeniedException`) and maps them to clean JSON payloads with appropriate HTTP status codes (`400`, `401`, `403`, `404`, `409`, `500`).

---

### Q15. How does React communicate with Spring Boot?
**Answer**:
React uses standard `fetch()` API calls defined in `frontend/src/api/index.js`. Vite dev server proxies `/api` requests to Spring Boot on `localhost:8080`. In production builds, React static assets (`index.html`, `assets/`) are compiled into `backend/src/main/resources/static` and served directly by Spring Boot.
