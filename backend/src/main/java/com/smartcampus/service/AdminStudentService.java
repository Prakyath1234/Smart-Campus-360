package com.smartcampus.service;

import com.smartcampus.dto.StudentDto;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminStudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminStudentService(StudentRepository studentRepository, UserRepository userRepository,
                               DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public StudentDto createStudent(StudentDto dto) {
        if (dto.getRollNumber() == null || dto.getRollNumber().trim().isEmpty()) {
            throw new BadRequestException("Roll number is required");
        }
        if (studentRepository.existsByRollNumber(dto.getRollNumber().trim())) {
            throw new BadRequestException("Roll number already exists: " + dto.getRollNumber());
        }

        User user;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + dto.getUserId()));
        } else {
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                throw new BadRequestException("Email is required for new student profile");
            }
            if (userRepository.existsByEmail(dto.getEmail().trim())) {
                throw new BadRequestException("Email already registered: " + dto.getEmail());
            }

            user = User.builder()
                    .name(dto.getName() != null ? dto.getName() : "Student " + dto.getRollNumber())
                    .email(dto.getEmail().trim().toLowerCase())
                    .password(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "password"))
                    .phone(dto.getPhone())
                    .role(Role.STUDENT)
                    .enabled(true)
                    .build();
            user = userRepository.save(user);
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));
        }

        Student student = Student.builder()
                .user(user)
                .rollNumber(dto.getRollNumber().trim().toUpperCase())
                .department(department)
                .semester(dto.getSemester() != null ? dto.getSemester() : 1)
                .enrollmentDate(dto.getEnrollmentDate() != null ? dto.getEnrollmentDate() : LocalDate.now())
                .build();

        return mapToDto(studentRepository.save(student));
    }

    public Page<StudentDto> getStudents(String search, Long departmentId, Integer semester, Pageable pageable) {
        Specification<Student> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Student, User> userJoin = root.join("user");

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(userJoin.get("name")), pattern);
                Predicate emailMatch = cb.like(cb.lower(userJoin.get("email")), pattern);
                Predicate rollMatch = cb.like(cb.lower(root.get("rollNumber")), pattern);
                predicates.add(cb.or(nameMatch, emailMatch, rollMatch));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            if (semester != null) {
                predicates.add(cb.equal(root.get("semester"), semester));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return studentRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return mapToDto(student);
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentDto dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));

        if (dto.getRollNumber() != null && !dto.getRollNumber().trim().isEmpty()) {
            String newRoll = dto.getRollNumber().trim().toUpperCase();
            if (!newRoll.equalsIgnoreCase(student.getRollNumber()) && studentRepository.existsByRollNumber(newRoll)) {
                throw new BadRequestException("Roll number already in use: " + newRoll);
            }
            student.setRollNumber(newRoll);
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));
            student.setDepartment(dept);
        }

        if (dto.getSemester() != null) {
            if (dto.getSemester() < 1 || dto.getSemester() > 12) {
                throw new BadRequestException("Invalid semester: must be between 1 and 12");
            }
            student.setSemester(dto.getSemester());
        }

        User user = student.getUser();
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        userRepository.save(user);

        return mapToDto(studentRepository.save(student));
    }

    @Transactional
    public StudentDto toggleStudentStatus(Long id, Boolean enabled) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        User user = student.getUser();
        user.setEnabled(enabled != null ? enabled : !user.isEnabled());
        userRepository.save(user);
        return mapToDto(student);
    }

    private StudentDto mapToDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .userId(student.getUser().getId())
                .name(student.getUser().getName())
                .email(student.getUser().getEmail())
                .phone(student.getUser().getPhone())
                .rollNumber(student.getRollNumber())
                .departmentId(student.getDepartment() != null ? student.getDepartment().getId() : null)
                .departmentName(student.getDepartment() != null ? student.getDepartment().getName() : null)
                .departmentCode(student.getDepartment() != null ? student.getDepartment().getCode() : null)
                .semester(student.getSemester())
                .enrollmentDate(student.getEnrollmentDate())
                .build();
    }
}
