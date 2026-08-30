package com.smartcampus;

import com.smartcampus.dto.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class AdminCrudTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminStudentService adminStudentService;

    @Autowired
    private AdminFacultyService adminFacultyService;

    @Autowired
    private PerformanceAnalyticsService performanceAnalyticsService;

    @Autowired
    private MentorshipService mentorshipService;

    @Autowired
    private ServiceRequestService serviceRequestService;

    @Autowired
    private EmergencyAlertService emergencyAlertService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private StatusHistoryService statusHistoryService;

    @Test
    void testUserCrudOperations() {
        UserDto newUser = UserDto.builder()
                .name("Alice AdminTest")
                .email("alice.test@sode-edu.in")
                .password("password123")
                .phone("1122334455")
                .role("STUDENT")
                .enabled(true)
                .build();

        UserDto created = adminUserService.createUser(newUser);
        assertNotNull(created.getId());
        assertEquals("Alice AdminTest", created.getName());
        assertEquals("alice.test@sode-edu.in", created.getEmail());
        assertNull(created.getPassword()); // Password must not be returned

        // Search user
        Page<UserDto> page = adminUserService.getUsers("Alice", "STUDENT", true, PageRequest.of(0, 10));
        assertTrue(page.getTotalElements() > 0);

        // Update user
        created.setName("Alice Updated");
        UserDto updated = adminUserService.updateUser(created.getId(), created);
        assertEquals("Alice Updated", updated.getName());

        // Toggle status
        UserDto disabled = adminUserService.toggleUserStatus(created.getId(), false);
        assertFalse(disabled.getEnabled());
    }

    @Test
    void testStudentCrudAndDuplicatePrevention() {
        StudentDto dto = StudentDto.builder()
                .name("Bob StudentTest")
                .email("bob.test@sode-edu.in")
                .password("password123")
                .rollNumber("CS999901")
                .semester(3)
                .build();

        StudentDto created = adminStudentService.createStudent(dto);
        assertNotNull(created.getId());
        assertEquals("CS999901", created.getRollNumber());

        // Duplicate Roll Number should throw BadRequestException
        StudentDto dup = StudentDto.builder()
                .name("Dup Student")
                .email("dup.test@sode-edu.in")
                .rollNumber("CS999901")
                .build();

        assertThrows(BadRequestException.class, () -> adminStudentService.createStudent(dup));
    }

    @Test
    void testFacultyCrudOperations() {
        FacultyDto dto = FacultyDto.builder()
                .name("Prof. Charles Test")
                .email("charles.test@sode-edu.in")
                .password("password123")
                .employeeId("EMP99901")
                .designation("Professor")
                .build();

        FacultyDto created = adminFacultyService.createFaculty(dto);
        assertNotNull(created.getId());
        assertEquals("EMP99901", created.getEmployeeId());
    }

    @Test
    void testPerformanceAnalyticsAndMentorship() {
        StudentPerformanceDto perf = performanceAnalyticsService.getPerformanceForStudentEmail("student@sode-edu.in");
        assertNotNull(perf);
        assertNotNull(perf.getRiskStatus());

        List<MentorshipDto> mentees = mentorshipService.getMenteesForFacultyEmail("faculty@sode-edu.in");
        assertNotNull(mentees);
        assertFalse(mentees.isEmpty());
    }

    @Test
    void testServiceRequestAndReports() {
        ServiceRequestDto req = ServiceRequestDto.builder()
                .requestType("BONAFIDE")
                .title("Bonafide Certificate for Bank Loan")
                .description("Requesting bonafide certificate for education loan processing.")
                .build();

        ServiceRequestDto created = serviceRequestService.createRequest(req, "student@sode-edu.in");
        assertNotNull(created.getId());
        assertEquals("PENDING", created.getStatus());

        // Test CSV Report Generation
        String csv = reportService.generateCsvReport("students");
        assertNotNull(csv);
        assertTrue(csv.contains("Roll Number"));
    }

    @Test
    void testEmergencyStateMachineAndAuditLogs() {
        EmergencyAlertDto sos = EmergencyAlertDto.builder()
                .emergencyType("MEDICAL")
                .description("Student severe migraine distress")
                .locationText("Campus Library 2nd Floor")
                .build();

        EmergencyAlertDto alert = emergencyAlertService.triggerSosForEmail(sos, "student@sode-edu.in");
        assertNotNull(alert.getId());
        assertEquals("ACTIVE", alert.getStatus());

        // Update status to ACKNOWLEDGED
        EmergencyAlertDto acked = emergencyAlertService.updateAlertStatus(alert.getId(), "ACKNOWLEDGED", null);
        assertEquals("ACKNOWLEDGED", acked.getStatus());

        // Update status to RESOLVED
        EmergencyAlertDto resolved = emergencyAlertService.updateAlertStatus(alert.getId(), "RESOLVED", null);
        assertEquals("RESOLVED", resolved.getStatus());

        // Transition from RESOLVED back to ACTIVE should fail state machine validation
        assertThrows(BadRequestException.class, () -> emergencyAlertService.updateAlertStatus(alert.getId(), "ACTIVE", null));

        // Audit Logs check
        Page<AuditLogDto> auditLogs = auditLogService.getAuditLogs(null, null, "EmergencyAlert", null, null, PageRequest.of(0, 10));
        assertTrue(auditLogs.getTotalElements() > 0);

        // Status History check
        List<StatusHistoryDto> history = statusHistoryService.getStatusHistory("EmergencyAlert", alert.getId());
        assertFalse(history.isEmpty());
    }
}
