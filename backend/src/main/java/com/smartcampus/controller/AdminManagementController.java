package com.smartcampus.controller;

import com.smartcampus.dto.*;
import com.smartcampus.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminManagementController {

    private final AdminUserService userService;
    private final AdminStudentService studentService;
    private final AdminFacultyService facultyService;

    public AdminManagementController(AdminUserService userService, AdminStudentService studentService,
                                     AdminFacultyService facultyService) {
        this.userService = userService;
        this.studentService = studentService;
        this.facultyService = facultyService;
    }

    // --- USER MANAGEMENT ---
    @PostMapping("/users")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(userService.getUsers(search, role, enabled, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id, @RequestParam(required = false) Boolean enabled) {
        return ResponseEntity.ok(userService.toggleUserStatus(id, enabled));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // --- STUDENT MANAGEMENT ---
    @PostMapping("/students")
    public ResponseEntity<StudentDto> createStudent(@RequestBody StudentDto dto) {
        return ResponseEntity.ok(studentService.createStudent(dto));
    }

    @GetMapping("/students")
    public ResponseEntity<Page<StudentDto>> getStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(studentService.getStudents(search, departmentId, semester, pageable));
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable Long id, @RequestBody StudentDto dto) {
        return ResponseEntity.ok(studentService.updateStudent(id, dto));
    }

    @PatchMapping("/students/{id}/status")
    public ResponseEntity<StudentDto> toggleStudentStatus(@PathVariable Long id, @RequestParam(required = false) Boolean enabled) {
        return ResponseEntity.ok(studentService.toggleStudentStatus(id, enabled));
    }

    // --- FACULTY MANAGEMENT ---
    @PostMapping("/faculties")
    public ResponseEntity<FacultyDto> createFaculty(@RequestBody FacultyDto dto) {
        return ResponseEntity.ok(facultyService.createFaculty(dto));
    }

    @GetMapping("/faculties")
    public ResponseEntity<Page<FacultyDto>> getFaculties(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(facultyService.getFaculty(search, departmentId, pageable));
    }

    @GetMapping("/faculties/{id}")
    public ResponseEntity<FacultyDto> getFacultyById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getFacultyById(id));
    }

    @PutMapping("/faculties/{id}")
    public ResponseEntity<FacultyDto> updateFaculty(@PathVariable Long id, @RequestBody FacultyDto dto) {
        return ResponseEntity.ok(facultyService.updateFaculty(id, dto));
    }

    @DeleteMapping("/faculties/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }
}
