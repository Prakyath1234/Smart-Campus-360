USE smart_campus_360;

-- Hashed BCrypt password for the string 'password': '$2a$10$8.Eng5rx3TvFnfiWd3F3c.E1s9u5V1.2NfJkC4h/K3Gcr3P5N6w92'

-- 1. Insert Users (Admin, Faculty, Student, Security) with sode-edu.in domain
INSERT INTO users (id, name, email, password, phone, role, enabled) VALUES
(1, 'System Administrator', 'admin@sode-edu.in', '$2a$10$8.Eng5rx3TvFnfiWd3F3c.E1s9u5V1.2NfJkC4h/K3Gcr3P5N6w92', '9876543210', 'ADMIN', TRUE),
(2, 'Dr. Sarah Jenkins', 'faculty@sode-edu.in', '$2a$10$8.Eng5rx3TvFnfiWd3F3c.E1s9u5V1.2NfJkC4h/K3Gcr3P5N6w92', '9876543211', 'FACULTY', TRUE),
(3, 'John Doe', 'student@sode-edu.in', '$2a$10$8.Eng5rx3TvFnfiWd3F3c.E1s9u5V1.2NfJkC4h/K3Gcr3P5N6w92', '9876543212', 'STUDENT', TRUE),
(4, 'Officer Chief Davis', 'security@sode-edu.in', '$2a$10$8.Eng5rx3TvFnfiWd3F3c.E1s9u5V1.2NfJkC4h/K3Gcr3P5N6w92', '9876543213', 'SECURITY', TRUE);

-- 2. Insert Departments
INSERT INTO departments (id, name, code) VALUES
(1, 'Computer Science and Engineering', 'CSE'),
(2, 'Electronics and Communication Engineering', 'ECE'),
(3, 'Mechanical Engineering', 'ME');

-- 3. Insert Students
INSERT INTO students (id, user_id, roll_number, department_id, semester, enrollment_date) VALUES
(1, 3, 'CS2026001', 1, 5, '2024-08-01');

-- 4. Insert Faculties
INSERT INTO faculties (id, user_id, employee_id, department_id, designation) VALUES
(1, 2, 'EMP2026101', 1, 'Associate Professor');

-- 5. Insert Subjects
INSERT INTO subjects (id, name, code, department_id, faculty_id, credits) VALUES
(1, 'Software Engineering', 'CS501', 1, 1, 4),
(2, 'Database Management Systems', 'CS502', 1, 1, 4),
(3, 'Computer Networks', 'CS503', 1, NULL, 3);

-- 6. Insert Timetables
INSERT INTO timetables (id, day_of_week, start_time, end_time, subject_id, classroom, semester) VALUES
(1, 'MONDAY', '09:00:00', '10:00:00', 1, 'Lab 1', 5),
(2, 'MONDAY', '10:00:00', '11:00:00', 2, 'Room 302', 5),
(3, 'WEDNESDAY', '11:15:00', '12:15:00', 1, 'Lab 1', 5),
(4, 'FRIDAY', '14:00:00', '15:00:00', 2, 'Room 302', 5);

-- 7. Insert Attendance Records
INSERT INTO attendance (id, student_id, subject_id, date, status) VALUES
(1, 1, 1, '2026-08-20', 'PRESENT'),
(2, 1, 1, '2026-08-21', 'PRESENT'),
(3, 1, 1, '2026-08-24', 'ABSENT'),
(4, 1, 2, '2026-08-20', 'PRESENT'),
(5, 1, 2, '2026-08-24', 'PRESENT');

-- 8. Insert Marks
INSERT INTO marks (id, student_id, subject_id, internal_marks, assignment_marks, exam_marks, total_marks, grade) VALUES
(1, 1, 1, 18.5, 9.0, 68.0, 95.5, 'A+'),
(2, 1, 2, 16.0, 8.5, 55.0, 79.5, 'B');

-- 9. Insert Leave Requests
INSERT INTO leave_requests (id, student_id, start_date, end_date, reason, status) VALUES
(1, 1, '2026-09-01', '2026-09-03', 'Family emergency and personal commitments.', 'PENDING');

-- 10. Insert Complaints
INSERT INTO complaints (id, student_id, title, description, category, priority, status) VALUES
(1, 1, 'Broken Chair in Lab 1', 'Several chairs in the Computer Science Lab 1 have broken wheels, making it uncomfortable during long coding sessions.', 'MAINTENANCE', 'LOW', 'OPEN'),
(2, 1, 'Exposed power cables near Server Room', 'There are multiple exposed power wires in the main corridor adjacent to the server room, which is a shock risk.', 'SAFETY', 'CRITICAL', 'OPEN');

-- 11. Insert Notifications
INSERT INTO notifications (id, user_id, title, message, type) VALUES
(1, 3, 'Welcome to Smart Campus 360', 'Your portal has been set up successfully. Feel free to explore your schedule and grades.', 'SYSTEM'),
(2, 3, 'New Subject Added', 'You have been enrolled in CS501: Software Engineering.', 'ACADEMIC');
