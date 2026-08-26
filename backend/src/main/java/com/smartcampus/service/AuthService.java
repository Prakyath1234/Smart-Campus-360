package com.smartcampus.service;

import com.smartcampus.dto.AuthRequest;
import com.smartcampus.dto.AuthResponse;
import com.smartcampus.dto.RegisterRequest;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NotificationService notificationService;

    public AuthService(UserRepository userRepository, StudentRepository studentRepository,
                       FacultyRepository facultyRepository, DepartmentRepository departmentRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.notificationService = notificationService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role specified. Must be ADMIN, FACULTY, STUDENT, or SECURITY");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .enabled(true)
                .build();

        user = userRepository.save(user);
        Long profileId = null;

        if (role == Role.STUDENT) {
            if (request.getRollNumber() == null || request.getRollNumber().trim().isEmpty()) {
                throw new BadRequestException("Roll number is required for students");
            }
            if (studentRepository.findByRollNumber(request.getRollNumber()).isPresent()) {
                throw new BadRequestException("Roll number already registered");
            }

            Department dept = null;
            if (request.getDepartmentId() != null) {
                dept = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new BadRequestException("Department not found"));
            }

            Student student = Student.builder()
                    .user(user)
                    .rollNumber(request.getRollNumber())
                    .department(dept)
                    .semester(request.getSemester() != null ? request.getSemester() : 1)
                    .enrollmentDate(LocalDate.now())
                    .build();
            student = studentRepository.save(student);
            profileId = student.getId();

        } else if (role == Role.FACULTY) {
            if (request.getEmployeeId() == null || request.getEmployeeId().trim().isEmpty()) {
                throw new BadRequestException("Employee ID is required for faculty");
            }
            if (facultyRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
                throw new BadRequestException("Employee ID already registered");
            }

            Department dept = null;
            if (request.getDepartmentId() != null) {
                dept = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new BadRequestException("Department not found"));
            }

            Faculty faculty = Faculty.builder()
                    .user(user)
                    .employeeId(request.getEmployeeId())
                    .department(dept)
                    .designation(request.getDesignation())
                    .build();
            faculty = facultyRepository.save(faculty);
            profileId = faculty.getId();
        }

        // Welcome notification
        notificationService.createNotification(user, "Welcome to Smart Campus 360",
                "Your account has been successfully registered. Role: " + role, NotificationType.SYSTEM);

        String jwt = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getName())
                .userId(user.getId())
                .profileId(profileId)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        Long profileId = null;
        if (user.getRole() == Role.STUDENT) {
            profileId = studentRepository.findByUserId(user.getId())
                    .map(Student::getId)
                    .orElse(null);
        } else if (user.getRole() == Role.FACULTY) {
            profileId = facultyRepository.findByUserId(user.getId())
                    .map(Faculty::getId)
                    .orElse(null);
        }

        String jwt = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .role(user.getRole().name())
                .name(user.getName())
                .userId(user.getId())
                .profileId(profileId)
                .build();
    }
}
