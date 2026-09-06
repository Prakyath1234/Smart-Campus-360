package com.smartcampus.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.dto.*;
import com.smartcampus.entity.Faculty;
import com.smartcampus.service.AcademicService;
import com.smartcampus.service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@CrossOrigin(origins = "*")
public class FacultyController {

    private final AcademicService academicService;
    private final LeaveRequestService leaveRequestService;
    private final ObjectMapper objectMapper;

    public FacultyController(AcademicService academicService, LeaveRequestService leaveRequestService) {
        this.academicService = academicService;
        this.leaveRequestService = leaveRequestService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @GetMapping("/profile")
    public ResponseEntity<FacultyDto> getFacultyProfile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(academicService.getAllFaculty().stream().findFirst().orElse(null));
        }
        try {
            return ResponseEntity.ok(academicService.getFacultyByEmail(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.ok(academicService.getAllFaculty().stream().findFirst().orElse(null));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<FacultyDto> getFacultyByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(academicService.getFacultyByUserId(userId));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectDto>> getFacultySubjects(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(academicService.getAllSubjects());
        }
        try {
            Faculty faculty = academicService.getFacultyEntityByEmail(principal.getName());
            return ResponseEntity.ok(academicService.getSubjectsByFacultyId(faculty.getId()));
        } catch (Exception e) {
            return ResponseEntity.ok(academicService.getAllSubjects());
        }
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentDto>> getFacultyStudents(@RequestParam(required = false) Long subjectId) {
        if (subjectId != null) {
            return ResponseEntity.ok(academicService.getStudentsBySubjectId(subjectId));
        }
        return ResponseEntity.ok(academicService.getAllStudents());
    }

    @GetMapping({"/classes", "/timetable"})
    public ResponseEntity<List<TimetableDto>> getFacultyClasses(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        try {
            return ResponseEntity.ok(academicService.getFacultyTimetableByEmail(principal.getName()));
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> recordAttendance(@RequestBody JsonNode node) {
        try {
            if (node.isArray()) {
                List<AttendanceDto> list = objectMapper.convertValue(node, new TypeReference<List<AttendanceDto>>() {});
                return ResponseEntity.ok(academicService.recordAttendanceBulk(list));
            } else {
                AttendanceDto dto = objectMapper.convertValue(node, AttendanceDto.class);
                return ResponseEntity.ok(academicService.recordAttendance(dto));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to record attendance"));
        }
    }

    @PostMapping("/attendance/bulk")
    public ResponseEntity<List<AttendanceDto>> recordAttendanceBulk(@RequestBody List<AttendanceDto> dtos) {
        return ResponseEntity.ok(academicService.recordAttendanceBulk(dtos));
    }

    @PostMapping("/marks")
    public ResponseEntity<MarksDto> enterMarks(@RequestBody MarksDto dto) {
        return ResponseEntity.ok(academicService.enterMarks(dto));
    }

    @GetMapping({"/leave-requests", "/leave-reviews", "/{id}/leave-reviews"})
    public ResponseEntity<List<LeaveRequestDto>> getFacultyLeaveRequests(@PathVariable(required = false) Long id, Principal principal) {
        if (id != null) {
            return ResponseEntity.ok(leaveRequestService.getFacultyLeaveRequests(id));
        }
        if (principal != null) {
            try {
                Faculty faculty = academicService.getFacultyEntityByEmail(principal.getName());
                return ResponseEntity.ok(leaveRequestService.getFacultyLeaveRequests(faculty.getId()));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(leaveRequestService.getAllLeaveRequests());
    }

    @PutMapping({"/leave-requests/{leaveId}", "/leave-reviews/{leaveId}"})
    public ResponseEntity<LeaveRequestDto> reviewLeave(
            @PathVariable Long leaveId,
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "") String comments,
            @RequestParam(required = false, defaultValue = "") String reviewComments,
            @RequestParam(required = false) Long facultyId,
            Principal principal
    ) {
        Long resolvedFacultyId = facultyId;
        if (resolvedFacultyId == null && principal != null) {
            try {
                Faculty faculty = academicService.getFacultyEntityByEmail(principal.getName());
                resolvedFacultyId = faculty.getId();
            } catch (Exception ignored) {}
        }
        if (resolvedFacultyId == null) {
            resolvedFacultyId = 1L;
        }
        String note = !reviewComments.isEmpty() ? reviewComments : comments;
        return ResponseEntity.ok(leaveRequestService.reviewLeave(leaveId, status, note, resolvedFacultyId));
    }
}
