package com.smartcampus.config;

import com.smartcampus.entity.*;
import com.smartcampus.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final TimetableRepository timetableRepository;
    private final MentorshipRepository mentorshipRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final AnnouncementRepository announcementRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(UserRepository userRepository, DepartmentRepository departmentRepository,
                               StudentRepository studentRepository, FacultyRepository facultyRepository,
                               SubjectRepository subjectRepository, TimetableRepository timetableRepository,
                               MentorshipRepository mentorshipRepository, ServiceRequestRepository serviceRequestRepository,
                               AnnouncementRepository announcementRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.timetableRepository = timetableRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.announcementRepository = announcementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        System.out.println("Initializing default database records with @sode-edu.in domain...");

        // 1. Create Departments
        Department cse = Department.builder().name("Computer Science and Engineering").code("CSE").build();
        Department ece = Department.builder().name("Electronics and Communication Engineering").code("ECE").build();
        Department me = Department.builder().name("Mechanical Engineering").code("ME").build();
        
        cse = departmentRepository.save(cse);
        ece = departmentRepository.save(ece);
        me = departmentRepository.save(me);

        String hashedPassword = passwordEncoder.encode("password");

        // 2. Create Users with sode-edu.in domain
        User adminUser = User.builder()
                .name("System Administrator")
                .email("admin@sode-edu.in")
                .password(hashedPassword)
                .phone("9876543210")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(adminUser);

        User facultyUser = User.builder()
                .name("Dr. Sarah Jenkins")
                .email("faculty@sode-edu.in")
                .password(hashedPassword)
                .phone("9876543211")
                .role(Role.FACULTY)
                .enabled(true)
                .build();

        User studentUser = User.builder()
                .name("John Doe")
                .email("student@sode-edu.in")
                .password(hashedPassword)
                .phone("9876543212")
                .role(Role.STUDENT)
                .enabled(true)
                .build();

        User securityUser = User.builder()
                .name("Officer Chief Davis")
                .email("security@sode-edu.in")
                .password(hashedPassword)
                .phone("9876543213")
                .role(Role.SECURITY)
                .enabled(true)
                .build();
        userRepository.save(securityUser);

        // 3. Create Student Profiles
        Student student = Student.builder()
                .user(studentUser)
                .rollNumber("CS2026001")
                .department(cse)
                .semester(5)
                .enrollmentDate(LocalDate.now())
                .build();
        student = studentRepository.save(student);

        User student2User = User.builder()
                .name("Alex Smith")
                .email("alex@sode-edu.in")
                .password(hashedPassword)
                .role(Role.STUDENT)
                .phone("9876543220")
                .enabled(true)
                .build();

        Student student2 = Student.builder()
                .user(student2User)
                .rollNumber("CS2026002")
                .department(cse)
                .semester(5)
                .enrollmentDate(LocalDate.now())
                .build();
        studentRepository.save(student2);

        User student3User = User.builder()
                .name("Priya Sharma")
                .email("priya@sode-edu.in")
                .password(hashedPassword)
                .role(Role.STUDENT)
                .phone("9876543221")
                .enabled(true)
                .build();

        Student student3 = Student.builder()
                .user(student3User)
                .rollNumber("CS2026003")
                .department(cse)
                .semester(5)
                .enrollmentDate(LocalDate.now())
                .build();
        studentRepository.save(student3);

        // 4. Create Faculty Profile
        Faculty faculty = Faculty.builder()
                .user(facultyUser)
                .employeeId("EMP2026101")
                .department(cse)
                .designation("Associate Professor")
                .build();
        faculty = facultyRepository.save(faculty);

        // 5. Create Subjects
        Subject se = Subject.builder().name("Software Engineering").code("CS501").department(cse).faculty(faculty).credits(4).build();
        Subject dbms = Subject.builder().name("Database Management Systems").code("CS502").department(cse).faculty(faculty).credits(4).build();
        Subject cn = Subject.builder().name("Computer Networks").code("CS503").department(cse).credits(3).build();

        se = subjectRepository.save(se);
        dbms = subjectRepository.save(dbms);
        cn = subjectRepository.save(cn);

        // Subjects for other semesters
        Subject dsa = Subject.builder().name("Data Structures & Algorithms").code("CS301").department(cse).credits(4).build();
        subjectRepository.save(dsa);
        Subject ai = Subject.builder().name("Artificial Intelligence").code("CS701").department(cse).credits(4).build();
        subjectRepository.save(ai);

        // 6. Create Timetable Slots (Semester 5 - Weekly schedule for Dr. Sarah Jenkins and CSE Sem 5)
        // MONDAY
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("MONDAY").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                .subject(se).classroom("Lab 1").semester(5).build());
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("MONDAY").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .subject(dbms).classroom("Room 302").semester(5).build());

        // TUESDAY
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("TUESDAY").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                .subject(dbms).classroom("Lab 2").semester(5).build());
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("TUESDAY").startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 0))
                .subject(se).classroom("Room 301").semester(5).build());

        // WEDNESDAY
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("WEDNESDAY").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .subject(se).classroom("Room 301").semester(5).build());
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("WEDNESDAY").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0))
                .subject(cn).classroom("Lab 3").semester(5).build());

        // THURSDAY
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("THURSDAY").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0))
                .subject(dbms).classroom("Room 302").semester(5).build());
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("THURSDAY").startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 0))
                .subject(se).classroom("Lab 1").semester(5).build());

        // FRIDAY
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("FRIDAY").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0))
                .subject(dbms).classroom("Room 302").semester(5).build());
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("FRIDAY").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0))
                .subject(cn).classroom("Room 204").semester(5).build());

        // Semester 3 Slots
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("MONDAY").startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 0))
                .subject(dsa).classroom("Room 101").semester(3).build());
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("WEDNESDAY").startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 0))
                .subject(dsa).classroom("Lab 2").semester(3).build());

        // Semester 7 Slots
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("TUESDAY").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0))
                .subject(ai).classroom("Seminar Hall").semester(7).build());
        timetableRepository.save(Timetable.builder()
                .dayOfWeek("THURSDAY").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0))
                .subject(ai).classroom("Lab 4").semester(7).build());

        // 7. Seed Mentorship
        Mentorship mentorship = Mentorship.builder()
                .mentor(faculty)
                .mentee(student)
                .build();
        mentorshipRepository.save(mentorship);

        // 8. Seed Service Request
        ServiceRequest req = ServiceRequest.builder()
                .student(student)
                .requestType("ID_CARD")
                .title("Replacement Smart Campus ID Card Request")
                .description("Damaged barcode on my student badge, request reissue.")
                .status("PENDING")
                .build();
        serviceRequestRepository.save(req);

        // 9. Seed Announcement
        Announcement ann = Announcement.builder()
                .title("Mid-Semester Academic Evaluation & Exam Schedule")
                .content("All Semester 5 CSE students are requested to report to Hall B for internal lab evaluations.")
                .targetAudience("ALL_STUDENTS")
                .targetDepartment(cse)
                .targetSemester(5)
                .authorName("Dr. Sarah Jenkins")
                .published(true)
                .build();
        announcementRepository.save(ann);

        System.out.println("Database initialization complete.");
    }
}
