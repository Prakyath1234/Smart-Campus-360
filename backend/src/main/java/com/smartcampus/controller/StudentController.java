package com.smartcampus.controller;

import com.smartcampus.dto.*;
import com.smartcampus.entity.Student;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.service.AcademicService;
import com.smartcampus.service.ComplaintService;
import com.smartcampus.service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping({"/api/student", "/api/students"})
@CrossOrigin(origins = "*")
public class StudentController {

    private final AcademicService academicService;
    private final LeaveRequestService leaveRequestService;
    private final ComplaintService complaintService;

    public StudentController(AcademicService academicService, LeaveRequestService leaveRequestService,
                             ComplaintService complaintService) {
        this.academicService = academicService;
        this.leaveRequestService = leaveRequestService;
        this.complaintService = complaintService;
    }

    private void validateStudentAccess(Long id, Principal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        StudentDto currentStudent = academicService.getStudentByEmail(principal.getName());
        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            boolean isStudent = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            if (isStudent && !currentStudent.getId().equals(id)) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You cannot access other students' records.");
            }
        }
    }

    private void validateUserAccess(Long userId, Principal principal) {
        if (principal == null) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        StudentDto currentStudent = academicService.getStudentByEmail(principal.getName());
        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            boolean isStudent = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            if (isStudent && !currentStudent.getUserId().equals(userId)) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied: You cannot access other users' student records.");
            }
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FACULTY')")
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        return ResponseEntity.ok(academicService.getAllStudents());
    }

    @GetMapping("/profile")
    public ResponseEntity<StudentDto> getProfile(Principal principal) {
        if (principal == null) {
            throw new BadRequestException("Unauthenticated request");
        }
        return ResponseEntity.ok(academicService.getStudentByEmail(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id, Principal principal) {
        validateStudentAccess(id, principal);
        return ResponseEntity.ok(academicService.getStudentById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StudentDto> getStudentByUserId(@PathVariable Long userId, Principal principal) {
        validateUserAccess(userId, principal);
        return ResponseEntity.ok(academicService.getStudentByUserId(userId));
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceDto>> getCurrentStudentAttendance(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Student student = academicService.getStudentEntityByEmail(principal.getName());
        return ResponseEntity.ok(academicService.getStudentAttendance(student.getId()));
    }

    @GetMapping("/{id}/attendance")
    public ResponseEntity<List<AttendanceDto>> getAttendance(@PathVariable Long id, Principal principal) {
        validateStudentAccess(id, principal);
        return ResponseEntity.ok(academicService.getStudentAttendance(id));
    }

    @GetMapping("/marks")
    public ResponseEntity<List<MarksDto>> getCurrentStudentMarks(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Student student = academicService.getStudentEntityByEmail(principal.getName());
        return ResponseEntity.ok(academicService.getStudentMarks(student.getId()));
    }

    @GetMapping("/{id}/marks")
    public ResponseEntity<List<MarksDto>> getMarks(@PathVariable Long id, Principal principal) {
        validateStudentAccess(id, principal);
        return ResponseEntity.ok(academicService.getStudentMarks(id));
    }

    @GetMapping("/timetable")
    public ResponseEntity<List<TimetableDto>> getCurrentStudentTimetable(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Student student = academicService.getStudentEntityByEmail(principal.getName());
        if (student.getSemester() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(academicService.getTimetableForSemester(student.getSemester()));
    }

    @GetMapping("/timetable/{semester}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FACULTY', 'ROLE_STUDENT')")
    public ResponseEntity<List<TimetableDto>> getTimetable(@PathVariable Integer semester) {
        return ResponseEntity.ok(academicService.getTimetableForSemester(semester));
    }

    @GetMapping({"/leave-requests", "/leave"})
    public ResponseEntity<List<LeaveRequestDto>> getCurrentStudentLeaves(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Student student = academicService.getStudentEntityByEmail(principal.getName());
        return ResponseEntity.ok(leaveRequestService.getStudentLeaves(student.getId()));
    }

    @GetMapping("/{id}/leave")
    public ResponseEntity<List<LeaveRequestDto>> getLeaves(@PathVariable Long id, Principal principal) {
        validateStudentAccess(id, principal);
        return ResponseEntity.ok(leaveRequestService.getStudentLeaves(id));
    }

    @PostMapping({"/leave-requests", "/leave"})
    public ResponseEntity<LeaveRequestDto> applyLeave(@RequestBody LeaveRequestDto dto, Principal principal) {
        if (principal == null) {
            throw new BadRequestException("Unauthenticated user cannot apply for leave");
        }
        if (dto.getReason() == null || dto.getReason().trim().isEmpty()) {
            throw new BadRequestException("Reason is required");
        }
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new BadRequestException("Start date and End date are required");
        }
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before or equal to End date");
        }

        Student student = academicService.getStudentEntityByEmail(principal.getName());
        dto.setStudentId(student.getId());
        return ResponseEntity.ok(leaveRequestService.applyLeave(dto));
    }

    @GetMapping({"/complaints", "/complaint"})
    public ResponseEntity<List<ComplaintDto>> getCurrentStudentComplaints(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Student student = academicService.getStudentEntityByEmail(principal.getName());
        return ResponseEntity.ok(complaintService.getStudentComplaints(student.getId()));
    }

    @GetMapping("/{id}/complaints")
    public ResponseEntity<List<ComplaintDto>> getComplaints(@PathVariable Long id, Principal principal) {
        validateStudentAccess(id, principal);
        return ResponseEntity.ok(complaintService.getStudentComplaints(id));
    }

    @PostMapping({"/complaints", "/complaint"})
    public ResponseEntity<ComplaintDto> submitComplaint(@RequestBody ComplaintDto dto, Principal principal) {
        if (principal == null) {
            throw new BadRequestException("Unauthenticated user cannot submit complaints");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Complaint title is required");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Complaint description is required");
        }

        Student student = academicService.getStudentEntityByEmail(principal.getName());
        dto.setStudentId(student.getId());
        return ResponseEntity.ok(complaintService.submitComplaint(dto));
    }
}
