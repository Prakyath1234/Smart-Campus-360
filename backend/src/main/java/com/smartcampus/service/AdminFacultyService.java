package com.smartcampus.service;

import com.smartcampus.dto.FacultyDto;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminFacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminFacultyService(FacultyRepository facultyRepository, UserRepository userRepository,
                               DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.facultyRepository = facultyRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public FacultyDto createFaculty(FacultyDto dto) {
        if (dto.getEmployeeId() == null || dto.getEmployeeId().trim().isEmpty()) {
            throw new BadRequestException("Employee ID is required");
        }
        if (facultyRepository.existsByEmployeeId(dto.getEmployeeId().trim())) {
            throw new BadRequestException("Employee ID already exists: " + dto.getEmployeeId());
        }

        User user;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + dto.getUserId()));
        } else {
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                throw new BadRequestException("Email is required for new faculty profile");
            }
            if (userRepository.existsByEmail(dto.getEmail().trim())) {
                throw new BadRequestException("Email already registered: " + dto.getEmail());
            }

            user = User.builder()
                    .name(dto.getName() != null ? dto.getName() : "Faculty " + dto.getEmployeeId())
                    .email(dto.getEmail().trim().toLowerCase())
                    .password(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "password"))
                    .phone(dto.getPhone())
                    .role(Role.FACULTY)
                    .enabled(true)
                    .build();
            user = userRepository.save(user);
        }

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));
        }

        Faculty faculty = Faculty.builder()
                .user(user)
                .employeeId(dto.getEmployeeId().trim().toUpperCase())
                .department(department)
                .designation(dto.getDesignation() != null ? dto.getDesignation() : "Assistant Professor")
                .build();

        return mapToDto(facultyRepository.save(faculty));
    }

    public Page<FacultyDto> getFaculty(String search, Long departmentId, Pageable pageable) {
        Specification<Faculty> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Faculty, User> userJoin = root.join("user");

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(userJoin.get("name")), pattern);
                Predicate emailMatch = cb.like(cb.lower(userJoin.get("email")), pattern);
                Predicate empMatch = cb.like(cb.lower(root.get("employeeId")), pattern);
                predicates.add(cb.or(nameMatch, emailMatch, empMatch));
            }

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return facultyRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    public FacultyDto getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + id));
        return mapToDto(faculty);
    }

    @Transactional
    public FacultyDto updateFaculty(Long id, FacultyDto dto) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + id));

        if (dto.getEmployeeId() != null && !dto.getEmployeeId().trim().isEmpty()) {
            String newEmpId = dto.getEmployeeId().trim().toUpperCase();
            if (!newEmpId.equalsIgnoreCase(faculty.getEmployeeId()) && facultyRepository.existsByEmployeeId(newEmpId)) {
                throw new BadRequestException("Employee ID already in use: " + newEmpId);
            }
            faculty.setEmployeeId(newEmpId);
        }

        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + dto.getDepartmentId()));
            faculty.setDepartment(dept);
        }

        if (dto.getDesignation() != null) {
            faculty.setDesignation(dto.getDesignation());
        }

        User user = faculty.getUser();
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        userRepository.save(user);

        return mapToDto(facultyRepository.save(faculty));
    }

    @Transactional
    public void deleteFaculty(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + id));
        facultyRepository.delete(faculty);
    }

    private FacultyDto mapToDto(Faculty faculty) {
        return FacultyDto.builder()
                .id(faculty.getId())
                .userId(faculty.getUser().getId())
                .name(faculty.getUser().getName())
                .email(faculty.getUser().getEmail())
                .phone(faculty.getUser().getPhone())
                .employeeId(faculty.getEmployeeId())
                .departmentId(faculty.getDepartment() != null ? faculty.getDepartment().getId() : null)
                .departmentName(faculty.getDepartment() != null ? faculty.getDepartment().getName() : null)
                .designation(faculty.getDesignation())
                .build();
    }
}
