package com.smartcampus.controller;

import com.smartcampus.dto.MentorNoteDto;
import com.smartcampus.dto.MentorshipDto;
import com.smartcampus.dto.StudentPerformanceDto;
import com.smartcampus.service.MentorshipService;
import com.smartcampus.service.PerformanceAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MentorshipController {

    private final MentorshipService mentorshipService;
    private final PerformanceAnalyticsService performanceAnalyticsService;

    public MentorshipController(MentorshipService mentorshipService,
                                PerformanceAnalyticsService performanceAnalyticsService) {
        this.mentorshipService = mentorshipService;
        this.performanceAnalyticsService = performanceAnalyticsService;
    }

    // --- STUDENT PERFORMANCE ANALYTICS ---
    @GetMapping("/student/performance")
    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<StudentPerformanceDto> getStudentPerformance(Principal principal) {
        return ResponseEntity.ok(performanceAnalyticsService.getPerformanceForStudentEmail(principal.getName()));
    }

    @GetMapping("/admin/analytics/at-risk")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<StudentPerformanceDto>> getAtRiskStudents() {
        return ResponseEntity.ok(performanceAnalyticsService.getAtRiskStudents());
    }

    // --- MENTORSHIP ASSIGNMENTS ---
    @PostMapping("/admin/mentorship/assign")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MentorshipDto> assignMentor(@RequestParam Long mentorId, @RequestParam Long menteeId) {
        return ResponseEntity.ok(mentorshipService.assignMentor(mentorId, menteeId));
    }

    @DeleteMapping("/admin/mentorship/{menteeId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> removeMentor(@PathVariable Long menteeId) {
        mentorshipService.removeMentor(menteeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/faculty/mentees")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY', 'ROLE_ADMIN')")
    public ResponseEntity<List<MentorshipDto>> getMentees(Principal principal) {
        return ResponseEntity.ok(mentorshipService.getMenteesForFacultyEmail(principal.getName()));
    }

    @GetMapping("/faculty/mentees/{studentId}/performance")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY', 'ROLE_ADMIN')")
    public ResponseEntity<StudentPerformanceDto> getMenteePerformance(@PathVariable Long studentId, Principal principal) {
        if (principal != null) {
            mentorshipService.verifyMentorMenteeRelationship(principal.getName(), studentId);
        }
        return ResponseEntity.ok(performanceAnalyticsService.getPerformanceForStudentId(studentId));
    }

    // --- MENTOR COUNSELING NOTES ---
    @PostMapping("/faculty/mentor-notes")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY', 'ROLE_ADMIN')")
    public ResponseEntity<MentorNoteDto> createNote(@RequestBody MentorNoteDto dto, Principal principal) {
        if (principal != null && dto != null && dto.getStudentId() != null) {
            mentorshipService.verifyMentorMenteeRelationship(principal.getName(), dto.getStudentId());
        }
        return ResponseEntity.ok(mentorshipService.createNote(dto, principal.getName()));
    }

    @GetMapping("/faculty/mentees/{studentId}/notes")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY', 'ROLE_ADMIN')")
    public ResponseEntity<List<MentorNoteDto>> getNotesForStudent(@PathVariable Long studentId, Principal principal) {
        if (principal != null) {
            mentorshipService.verifyMentorMenteeRelationship(principal.getName(), studentId);
        }
        return ResponseEntity.ok(mentorshipService.getNotesForStudent(studentId));
    }

    @PutMapping("/faculty/mentor-notes/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY', 'ROLE_ADMIN')")
    public ResponseEntity<MentorNoteDto> updateNote(@PathVariable Long id, @RequestParam String noteText,
                                                     @RequestParam(required = false) String category, Principal principal) {
        return ResponseEntity.ok(mentorshipService.updateNote(id, noteText, category, principal.getName()));
    }

    @DeleteMapping("/faculty/mentor-notes/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, Principal principal) {
        mentorshipService.deleteNote(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
