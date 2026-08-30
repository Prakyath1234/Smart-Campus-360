package com.smartcampus;

import com.smartcampus.dto.AuthRequest;
import com.smartcampus.dto.AuthResponse;
import com.smartcampus.dto.RegisterRequest;
import com.smartcampus.dto.TimetableDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.AcademicService;
import com.smartcampus.service.AiClassificationService;
import com.smartcampus.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2") // Run tests against H2 in-memory profile
@Transactional
class SmartCampusApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private AiClassificationService aiClassificationService;

    @Autowired
    private AcademicService academicService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    private Department csDept;

    @BeforeEach
    void setUp() {
        csDept = Department.builder()
                .name("Computer Science")
                .code("CSE_TEST")
                .build();
        csDept = departmentRepository.save(csDept);
    }

    @Test
    void testUserRegistrationAndLogin() {
        RegisterRequest register = new RegisterRequest();
        register.setName("Test Student");
        register.setEmail("teststudent@sode-edu.in");
        register.setPassword("password123");
        register.setRole("STUDENT");
        register.setRollNumber("ROLL999");
        register.setDepartmentId(csDept.getId());
        register.setSemester(5);

        AuthResponse regResponse = authService.register(register);
        assertNotNull(regResponse);
        assertNotNull(regResponse.getToken());
        assertEquals("teststudent@sode-edu.in", regResponse.getEmail());
        assertEquals("STUDENT", regResponse.getRole());

        // Test login
        AuthRequest login = new AuthRequest("teststudent@sode-edu.in", "password123");
        AuthResponse loginResponse = authService.login(login);
        assertNotNull(loginResponse);
        assertEquals(regResponse.getToken().substring(0, 10), loginResponse.getToken().substring(0, 10)); // simple token similarity check
    }

    @Test
    void testAiComplaintClassification() {
        // Test Safety Category
        AiClassificationService.ClassificationResult result1 = 
                aiClassificationService.classify("Exposed bare wires in the chemistry laboratory near the sink.");
        assertEquals(ComplaintCategory.SAFETY, result1.getCategory());
        assertEquals(ComplaintPriority.CRITICAL, result1.getPriority());

        // Test Maintenance Category
        AiClassificationService.ClassificationResult result2 = 
                aiClassificationService.classify("The toilet faucet is leaking water constantly in the first floor bathroom.");
        assertEquals(ComplaintCategory.MAINTENANCE, result2.getCategory());
        assertEquals(ComplaintPriority.HIGH, result2.getPriority());

        // Test Academic Category
        AiClassificationService.ClassificationResult result3 = 
                aiClassificationService.classify("I want to raise a complaint about the grading criteria of Software Engineering exam.");
        assertEquals(ComplaintCategory.ACADEMIC, result3.getCategory());
    }

    @Test
    void testTimetableOverlapConflict() {
        Subject subject = Subject.builder()
                .name("Software Engineering Test")
                .code("CS999")
                .department(csDept)
                .credits(4)
                .build();
        subject = subjectRepository.save(subject);

        // Add first slot
        TimetableDto dto1 = TimetableDto.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .subjectId(subject.getId())
                .classroom("Room 101")
                .semester(5)
                .build();
        academicService.createTimetableSlot(dto1);

        // Add second slot with overlapping times in same classroom (Conflict!)
        TimetableDto dto2 = TimetableDto.builder()
                .dayOfWeek("MONDAY")
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .subjectId(subject.getId())
                .classroom("Room 101")
                .semester(5)
                .build();

        assertThrows(BadRequestException.class, () -> {
            academicService.createTimetableSlot(dto2);
        });
    }
}
