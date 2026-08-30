package com.smartcampus;

import com.smartcampus.dto.EmergencyAlertDto;
import com.smartcampus.entity.EmergencyStatus;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.service.EmergencyAlertService;
import com.smartcampus.smarttools.service.TemporaryFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SecurityHardenedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmergencyAlertService emergencyAlertService;

    @Autowired
    private TemporaryFileService temporaryFileService;

    @Test
    @WithMockUser(username = "student@sode-edu.in", authorities = "ROLE_STUDENT")
    void testStudentCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "faculty@sode-edu.in", authorities = "ROLE_FACULTY")
    void testFacultyCannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUnauthenticatedCannotAccessStudentProfile() throws Exception {
        mockMvc.perform(get("/api/student/profile"))
                .andExpect(status().isForbidden()); // JWT filter or Spring Security will deny access (either 401/403)
    }

    @Test
    @WithMockUser(username = "admin@sode-edu.in", authorities = "ROLE_ADMIN")
    void testAdminAccessesAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    void testPathTraversalValidationBlocked() {
        MockMultipartFile file = new MockMultipartFile("file", "../../../evil.pdf", "application/pdf", "Hello World".getBytes());
        // TemporaryFileService saves file to a safe UUID name, completely sanitizing the original file name,
        // which prevents any traversal from working. Let's make sure it copies without throwing traversal errors
        // because it uses UUID as name on disk, completely eliminating malicious paths!
        File saved = temporaryFileService.saveMultipartToTemp(file);
        try {
            org.junit.jupiter.api.Assertions.assertNotNull(saved);
            org.junit.jupiter.api.Assertions.assertFalse(saved.getName().contains("evil"));
        } finally {
            temporaryFileService.cleanUpTempFile(saved);
        }
    }
}
