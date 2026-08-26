package com.smartcampus.controller;

import com.smartcampus.dto.*;
import com.smartcampus.service.AcademicService;
import com.smartcampus.service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@CrossOrigin(origins = "*")
public class FacultyController {

    private final AcademicService academicService;
    private final LeaveRequestService leaveRequestService;

    public FacultyController(AcademicService academicService, LeaveRequestService leaveRequestService) {
        this.academicService = academicService;
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<FacultyDto> getFacultyByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(academicService.getFacultyByUserId(userId));
    }

    @PostMapping("/attendance")
    public ResponseEntity<AttendanceDto> recordAttendance(@RequestBody AttendanceDto dto) {
        return ResponseEntity.ok(academicService.recordAttendance(dto));
    }

    @PostMapping("/marks")
    public ResponseEntity<MarksDto> enterMarks(@RequestBody MarksDto dto) {
        return ResponseEntity.ok(academicService.enterMarks(dto));
    }

    @GetMapping("/{id}/leave-reviews")
    public ResponseEntity<List<LeaveRequestDto>> getLeaveReviews(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getFacultyLeaveRequests(id));
    }

    @PutMapping("/leave-reviews/{leaveId}")
    public ResponseEntity<LeaveRequestDto> reviewLeave(
            @PathVariable Long leaveId,
            @RequestParam String status,
            @RequestParam String comments,
            @RequestParam Long facultyId
    ) {
        return ResponseEntity.ok(leaveRequestService.reviewLeave(leaveId, status, comments, facultyId));
    }
}
