CREATE DATABASE IF NOT EXISTS smart_campus_360;
USE smart_campus_360;

-- Drop tables in reverse order of dependencies to avoid foreign key errors
DROP TABLE IF EXISTS emergency_alerts;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS complaints;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS timetables;
DROP TABLE IF EXISTS marks;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS faculties;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS users;

-- 1. Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- ADMIN, FACULTY, STUDENT, SECURITY
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
);

-- 2. Departments table
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE
);

-- 3. Students table
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    roll_number VARCHAR(20) NOT NULL UNIQUE,
    department_id BIGINT,
    semester INT NOT NULL,
    enrollment_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- 4. Faculties table
CREATE TABLE faculties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(20) NOT NULL UNIQUE,
    department_id BIGINT,
    designation VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- 5. Subjects table
CREATE TABLE subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    department_id BIGINT,
    faculty_id BIGINT,
    credits INT NOT NULL DEFAULT 3,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE SET NULL
);

-- 6. Attendance table
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(10) NOT NULL, -- PRESENT, ABSENT
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_subject_date (student_id, subject_id, date)
);

-- 7. Marks table
CREATE TABLE marks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    internal_marks DOUBLE DEFAULT 0,
    assignment_marks DOUBLE DEFAULT 0,
    exam_marks DOUBLE DEFAULT 0,
    total_marks DOUBLE DEFAULT 0,
    grade VARCHAR(2),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_subject_marks (student_id, subject_id)
);

-- 8. Timetable table
CREATE TABLE timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    day_of_week VARCHAR(15) NOT NULL, -- MONDAY, TUESDAY, etc.
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    subject_id BIGINT NOT NULL,
    classroom VARCHAR(50) NOT NULL,
    semester INT NOT NULL,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

-- 9. Leave Requests table
CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    reviewed_by_faculty_id BIGINT,
    review_comments VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_faculty_id) REFERENCES faculties(id) ON DELETE SET NULL
);

-- 10. Complaints table
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(30) NOT NULL, -- ACADEMIC, INFRASTRUCTURE, MAINTENANCE, HOSTEL, TRANSPORT, SAFETY, OTHER
    priority VARCHAR(15) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolution_comments VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- 11. Notifications table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 12. Emergency Alerts table
CREATE TABLE emergency_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    emergency_type VARCHAR(50) NOT NULL,
    description TEXT,
    latitude DOUBLE,
    longitude DOUBLE,
    location_text VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, ACKNOWLEDGED, IN_PROGRESS, RESOLVED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    resolved_by_user_id BIGINT,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (resolved_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);
