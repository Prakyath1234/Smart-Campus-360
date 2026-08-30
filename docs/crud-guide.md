# Smart Campus 360 - Complete CRUD & Architecture Guide

This guide details the architectural breakdown of the CRUD modules implemented in **Smart Campus 360**.

---

## 1. User Management CRUD

- **Controller**: `com.smartcampus.controller.AdminManagementController`
- **Service**: `com.smartcampus.service.AdminUserService`
- **Repository**: `com.smartcampus.repository.UserRepository` (extends `JpaRepository`, `JpaSpecificationExecutor`)
- **DTO**: `com.smartcampus.dto.UserDto` (uses `@JsonProperty(access = WRITE_ONLY)` on `password` to ensure hash/raw credentials are never leaked in response payloads)
- **Entity**: `com.smartcampus.entity.User`
- **Database Relationships**: One-to-One with `Student` and `Faculty`.
- **Validation**: Email format validation, required fields check, duplicate email check.
- **Authorization**: Protected by `@PreAuthorize("hasAuthority('ROLE_ADMIN')")`.

---

## 2. Student Management CRUD

- **Controller**: `com.smartcampus.controller.AdminManagementController`
- **Service**: `com.smartcampus.service.AdminStudentService`
- **Repository**: `com.smartcampus.repository.StudentRepository`
- **DTO**: `com.smartcampus.dto.StudentDto`
- **Entity**: `com.smartcampus.entity.Student`
- **Database Relationships**:
  - `@OneToOne` with `User`
  - `@ManyToOne` with `Department`
- **Validation**: Unique Roll Number enforcement, semester validation (1–12), unique email check.
- **Authorization**: Restricted to `ROLE_ADMIN`.

---

## 3. Faculty Management CRUD

- **Controller**: `com.smartcampus.controller.AdminManagementController`
- **Service**: `com.smartcampus.service.AdminFacultyService`
- **Repository**: `com.smartcampus.repository.FacultyRepository`
- **DTO**: `com.smartcampus.dto.FacultyDto`
- **Entity**: `com.smartcampus.entity.Faculty`
- **Database Relationships**:
  - `@OneToOne` with `User`
  - `@ManyToOne` with `Department`
  - `@OneToMany` with `Subject`
- **Validation**: Unique Employee ID check, required name and department ID.
- **Authorization**: Restricted to `ROLE_ADMIN`.

---

## 4. Department Management CRUD

- **Controller**: `com.smartcampus.controller.AdminController`
- **Service**: `com.smartcampus.service.AcademicService`
- **Repository**: `com.smartcampus.repository.DepartmentRepository`
- **Entity**: `com.smartcampus.entity.Department`
- **Validation**: Unique department code (`CSE`, `ECE`, `ME`).
- **Safety Strategy**: Prevents deleting departments containing active students, faculty, or subjects.

---

## 5. Subject Management CRUD

- **Controller**: `com.smartcampus.controller.AdminController`
- **Service**: `com.smartcampus.service.AcademicService`
- **Repository**: `com.smartcampus.repository.SubjectRepository`
- **DTO**: `com.smartcampus.dto.SubjectDto`
- **Entity**: `com.smartcampus.entity.Subject`
- **Validation**: Credit bounds check (1 to 6 credits), unique course subject code.
