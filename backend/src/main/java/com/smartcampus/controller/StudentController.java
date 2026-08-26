package com.smartcampus.controller;

import com.smartcampus.dto.*;
import com.smartcampus.service.AcademicService;
import com.smartcampus.service.ComplaintService;
import com.smartcampus.service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
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

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        return ResponseEntity.ok(academicService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(academicService.getStudentById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<StudentDto> getStudentByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(academicService.getStudentByUserId(userId));
    }

    @GetMapping("/{id}/attendance")
    public ResponseEntity<List<AttendanceDto>> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(academicService.getStudentAttendance(id));
    }

    @GetMapping("/{id}/marks")
    public ResponseEntity<List<MarksDto>> getMarks(@PathVariable Long id) {
        return ResponseEntity.ok(academicService.getStudentMarks(id));
    }

    @GetMapping("/timetable/{semester}")
    public ResponseEntity<List<TimetableDto>> getTimetable(@PathVariable Integer semester) {
        return ResponseEntity.ok(academicService.getTimetableForSemester(semester));
    }

    @PostMapping("/leave")
    public ResponseEntity<LeaveRequestDto> applyLeave(@RequestBody LeaveRequestDto dto) {
        return ResponseEntity.ok(leaveRequestService.applyLeave(dto));
    }

    @GetMapping("/{id}/leave")
    public ResponseEntity<List<LeaveRequestDto>> getLeaves(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getStudentLeaves(id));
    }

    @PostMapping("/complaint")
    public ResponseEntity<ComplaintDto> submitComplaint(@RequestBody ComplaintDto dto) {
        return ResponseEntity.ok(complaintService.submitComplaint(dto));
    }

    @GetMapping("/{id}/complaints")
    public ResponseEntity<List<ComplaintDto>> getComplaints(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getStudentComplaints(id));
    }
}
