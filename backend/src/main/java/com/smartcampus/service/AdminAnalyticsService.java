package com.smartcampus.service;

import com.smartcampus.entity.*;
import com.smartcampus.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAnalyticsService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final ComplaintRepository complaintRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final PerformanceAnalyticsService performanceAnalyticsService;

    public AdminAnalyticsService(StudentRepository studentRepository, FacultyRepository facultyRepository,
                                 DepartmentRepository departmentRepository, SubjectRepository subjectRepository,
                                 ComplaintRepository complaintRepository, LeaveRequestRepository leaveRequestRepository,
                                 ServiceRequestRepository serviceRequestRepository, EmergencyAlertRepository emergencyAlertRepository,
                                 AttendanceRepository attendanceRepository, MarksRepository marksRepository,
                                 PerformanceAnalyticsService performanceAnalyticsService) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.complaintRepository = complaintRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.performanceAnalyticsService = performanceAnalyticsService;
    }

    public Map<String, Object> getOverviewAnalytics() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("studentCount", studentRepository.count());
        overview.put("facultyCount", facultyRepository.count());
        overview.put("departmentCount", departmentRepository.count());
        overview.put("subjectCount", subjectRepository.count());
        overview.put("activeComplaints", complaintRepository.countByStatus(ComplaintStatus.OPEN));
        overview.put("pendingLeaves", leaveRequestRepository.countByStatus(LeaveStatus.PENDING));

        long pendingServices = serviceRequestRepository.findAll().stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()))
                .count();
        overview.put("pendingServiceRequests", pendingServices);
        overview.put("activeEmergencyAlerts", emergencyAlertRepository.countByStatus(EmergencyStatus.ACTIVE));
        overview.put("atRiskStudents", performanceAnalyticsService.getAtRiskStudents().size());

        List<Attendance> allAttendance = attendanceRepository.findAll();
        long totalAtt = allAttendance.size();
        long presentAtt = allAttendance.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        double avgAttPct = totalAtt > 0 ? ((double) presentAtt / totalAtt) * 100.0 : 100.0;
        overview.put("averageAttendance", Math.round(avgAttPct * 10.0) / 10.0);

        List<Marks> allMarks = marksRepository.findAll();
        double avgScore = allMarks.isEmpty() ? 0.0 :
                allMarks.stream().mapToDouble(m -> m.getTotalMarks() != null ? m.getTotalMarks() : 0.0).average().orElse(0.0);
        overview.put("averageMarks", Math.round(avgScore * 10.0) / 10.0);

        return overview;
    }
}
