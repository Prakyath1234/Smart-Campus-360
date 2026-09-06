package com.smartcampus.controller;

import com.smartcampus.dto.ComplaintDto;
import com.smartcampus.dto.LeaveRequestDto;
import com.smartcampus.dto.SubjectDto;
import com.smartcampus.dto.TimetableDto;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.LeaveStatus;
import com.smartcampus.entity.ComplaintStatus;
import com.smartcampus.entity.EmergencyStatus;
import com.smartcampus.repository.*;
import com.smartcampus.service.AcademicService;
import com.smartcampus.service.ComplaintService;
import com.smartcampus.service.LeaveRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AcademicService academicService;
    private final ComplaintService complaintService;
    private final LeaveRequestService leaveRequestService;
    
    // Repositories injected directly for lightweight dashboard count queries
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final ComplaintRepository complaintRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;

    public AdminController(AcademicService academicService, ComplaintService complaintService,
                           LeaveRequestService leaveRequestService, StudentRepository studentRepository,
                           FacultyRepository facultyRepository, DepartmentRepository departmentRepository,
                           SubjectRepository subjectRepository, ComplaintRepository complaintRepository,
                           LeaveRequestRepository leaveRequestRepository, EmergencyAlertRepository emergencyAlertRepository) {
        this.academicService = academicService;
        this.complaintService = complaintService;
        this.leaveRequestService = leaveRequestService;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.complaintRepository = complaintRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
    }

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(academicService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody Department dept) {
        return ResponseEntity.ok(academicService.createDepartment(dept));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        departmentRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectDto> createSubject(@RequestBody SubjectDto dto) {
        return ResponseEntity.ok(academicService.createSubject(dto));
    }

    @PostMapping("/timetable")
    public ResponseEntity<TimetableDto> createTimetableSlot(@RequestBody TimetableDto dto) {
        return ResponseEntity.ok(academicService.createTimetableSlot(dto));
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintDto>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @PutMapping("/complaints/{id}")
    public ResponseEntity<ComplaintDto> updateComplaint(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "") String comments
    ) {
        return ResponseEntity.ok(complaintService.updateComplaintStatus(id, status, comments));
    }

    @GetMapping("/leaves")
    public ResponseEntity<List<LeaveRequestDto>> getAllLeaves() {
        return ResponseEntity.ok(leaveRequestService.getAllLeaveRequests());
    }

    @GetMapping({"/dashboard", "/stats"})
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalFaculty", facultyRepository.count());
        stats.put("totalDepartments", departmentRepository.count());
        stats.put("totalSubjects", subjectRepository.count());
        stats.put("pendingComplaints", complaintRepository.countByStatus(ComplaintStatus.OPEN));
        stats.put("pendingLeaves", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));
        stats.put("activeEmergencies", emergencyAlertRepository.countByStatus(EmergencyStatus.ACTIVE));
        return ResponseEntity.ok(stats);
    }
}
