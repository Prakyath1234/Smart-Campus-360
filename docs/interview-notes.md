# Smart Campus 360 - Technical Interview Guide & Preparation Notes

## Technical Architecture Q&A

### 1. Why Java?
Java offers high performance, strict compile-time type-safety, robust multithreading capabilities, and an active enterprise ecosystem, making it the standard choice for secure backends.

### 2. Why Spring Boot?
Spring Boot minimizes boilerplate configuration through starter dependencies and auto-configuration mechanisms, enabling fast prototyping and embedded server startup out-of-the-box.

### 3. Why MySQL?
MySQL is a proven, ACID-compliant relational database. Campus databases are highly structured (attendance records relate to students and courses), making transactional integrity and relational constraints vital.

### 4. What is JPA & Hibernate?
- **JPA (Jakarta Persistence API):** A specification defining Object-Relational Mapping (ORM) inside Java.
- **Hibernate:** The default ORM engine implementation that translates Java objects to SQL statements at runtime.

### 5. What is REST API?
Representational State Transfer (REST) is an architectural style for design APIs using HTTP protocols (GET, POST, PUT, DELETE) transmitting JSON payloads.

### 6. What is Dependency Injection?
A design pattern where objects receive their dependencies from the Spring IoC container (using `@Autowired` or constructor injection) rather than instantiating them locally, promoting loose coupling.

### 7. What is Spring Security & JWT?
- **Spring Security:** A framework providing security filters for request authentication and role-based route authorizations.
- **JWT (JSON Web Token):** A stateless token signed with a secret key containing claims (username, role) representing session status.

### 8. Authentication vs. Authorization
- **Authentication:** Verifying *who* the user is (e.g. login credentials check).
- **Authorization:** Verifying *what* permissions the authenticated user has (e.g. `FACULTY` role allowed to mark attendance).

### 9. Why BCrypt?
BCrypt is a slow hashing algorithm that includes an auto-generated salt, protecting passwords from rainbow-table lookups and brute-force GPU attacks.

### 10. Why DTO?
Data Transfer Objects decouple the database schema (entities) from the public REST layer, preventing JSON recursive serialization issues and hiding internal schema structures.

### 11. How does a request travel through the application?
1. Browser hits URL with header `Authorization: Bearer <JWT>`.
2. `JwtAuthenticationFilter` interceptor extracts and validates the token.
3. Security context is populated with custom UserDetails.
4. Controller matches route, parses DTO payload, and calls Service layer.
5. Service executes business transactions, committing through JPA Repository.
6. DB commits; controller formats returned DTOs to JSON.

### 12. How does the Emergency SOS work?
1. Student clicks SOS, browser extracts location coordinates.
2. Request POSTs to `/api/emergency/sos`.
3. Saved Alert triggers immediate notifications populated in Security/Admin inboxes.

---

## 30 Potential Interview Questions & Answers

1. **How does Spring Boot resolve active profiles?**
   By passing `-Dspring.profiles.active=h2` during startup, it overrides standard values with configuration keys in `application-h2.properties`.
   
2. **What does `@Transactional` do?**
   It defines transaction boundaries. If any runtime exception is thrown during execution, database changes are automatically rolled back.

3. **How do you prevent SQL injection in JPA?**
   JPA uses parameterized queries by default (prepared statements), preventing raw SQL concatenation and sanitizing inputs.

4. **Why choose constructor injection over field injection?**
   Constructor injection facilitates immutability (`final` fields) and allows easy unit testing since mock services can be passed directly to constructors.

5. **How does JWT verification avoid database queries?**
   By validating the cryptographic signature using the shared secret key. If the signature matches, the claims (role, email) are trusted without hitting the DB.

6. **What is `@RestControllerAdvice`?**
   A global interceptor that handles exceptions thrown by any Controller and maps them to clean JSON responses.

7. **How does Lombok work?**
   Lombok processes annotations (like `@Data`) during compile time and injects getters, setters, and constructors directly into the bytecode.

8. **What is the difference between `ddl-auto=update` and `create-drop`?**
   `update` updates tables as JPA schema matches without erasing existing records. `create-drop` drops tables on shutdown (ideal for tests).

9. **What is CORS?**
   Cross-Origin Resource Sharing is a browser mechanism that blocks web applications hosted on one origin (e.g., file system / port 3000) from requesting resources on another (e.g., port 8080) unless explicit headers are set.

10. **Explain how timetable conflicts are checked.**
    Queries scan slots in the database matching the requested day, searching for overlapping time ranges for the same classroom, faculty, or section/semester.

11. **How do you handle password salts?**
    BCrypt embeds the salt inside the final hashed output string, eliminating the need to store salts in a separate column.

12. **Why use H2 database during testing?**
    H2 runs in-memory, requiring zero installation, making it extremely fast for integration testing without polluting MySQL.

13. **What is the role of `OncePerRequestFilter`?**
    It ensures that a security filter runs exactly once per servlet request, preventing redundant executions in nested dispatches.

14. **What is the use of `@Column(nullable = false)`?**
    It sets database column constraints to `NOT NULL` during DDL creation, acting as a secondary validation layer.

15. **What is Jakarta Bean Validation?**
    A specification that checks properties (e.g., `@Email`, `@NotBlank`) on controllers before processing request payloads.

16. **Why do we use `@CreationTimestamp`?**
    It allows Hibernate to automatically set timestamps when inserting new records, simplifying audit trails.

17. **What is the use of `WebSecurityConfigurerAdapter` in Spring Security 6?**
    It is deprecated. Security filters are now configured using a `SecurityFilterChain` bean.

18. **How does the AI Classifier categorize complaints?**
    Through keyword lookup strings matching categories. E.g. "leak" matches `MAINTENANCE` and "wire" matches `SAFETY` with `CRITICAL` priority.

19. **What are JPA relationship fetch types?**
    - `LAZY`: Mapped values are loaded on demand.
    - `EAGER`: Related tables are joined and loaded immediately.

20. **Why avoid bidirectional `@ManyToMany` in REST entities?**
    It can trigger infinite JSON loops during serialization. Decoupling via DTOs resolves this completely.

21. **What is stateless session management?**
    The server does not store user session data on memory. Each request must carry credentials (JWT), enabling easier horizontal scaling.

22. **What is the purpose of `@ResponseStatus`?**
    It maps a custom Java exception to a specific HTTP status code, such as `404 NOT FOUND`.

23. **What is a MockMvc?**
    A Spring Boot test class tool that mocks HTTP requests to REST controllers without booting a full servlet container.

24. **How do you structure validation errors?**
    `GlobalExceptionHandler` intercepts validation failures and returns a map of field names to their specific error messages.

25. **Why Decouple User from Student/Faculty?**
    It allows general users (like administrators and security staff) to exist without requiring academic fields like roll numbers.

26. **What is `createDatabaseIfNotExist=true` query parameter?**
    It tells the MySQL driver to automatically run a DB initialization command if the schema database doesn't exist yet.

27. **What does Hibernate `@GeneratedValue` do?**
    Delegates primary key sequence increment operations to the database (using `AUTO_INCREMENT` in MySQL).

28. **How do you configure CORS in Spring Security?**
    By registering a `CorsConfigurationSource` bean allowing methods (GET, POST) and headers (Authorization) from client origins.

29. **What does `POM` stand for in Maven?**
    Project Object Model. It defines build directories, versions, plugins, and dependencies.

30. **Explain how notifications are routed.**
    Upon creating events (like leave requests), the system fetches appropriate recipients and registers rows in the notification table. The client polls or pulls notifications on reload.
