package com.smartcampus.service;

import com.smartcampus.dto.MentorNoteDto;
import com.smartcampus.dto.MentorshipDto;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.MentorNote;
import com.smartcampus.entity.Mentorship;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.entity.Role;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.MentorNoteRepository;
import com.smartcampus.repository.MentorshipRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentorshipService {

    private final MentorshipRepository mentorshipRepository;
    private final MentorNoteRepository mentorNoteRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public MentorshipService(MentorshipRepository mentorshipRepository, MentorNoteRepository mentorNoteRepository,
                             FacultyRepository facultyRepository, StudentRepository studentRepository,
                             UserRepository userRepository) {
        this.mentorshipRepository = mentorshipRepository;
        this.mentorNoteRepository = mentorNoteRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MentorshipDto assignMentor(Long mentorId, Long menteeId) {
        Faculty faculty = facultyRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + mentorId));
        Student student = studentRepository.findById(menteeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + menteeId));

        if (mentorshipRepository.existsByMenteeId(menteeId)) {
            Mentorship existing = mentorshipRepository.findByMenteeId(menteeId).get();
            existing.setMentor(faculty);
            return mapToDto(mentorshipRepository.save(existing));
        }

        Mentorship mentorship = Mentorship.builder()
                .mentor(faculty)
                .mentee(student)
                .build();

        return mapToDto(mentorshipRepository.save(mentorship));
    }

    @Transactional
    public void removeMentor(Long menteeId) {
        Mentorship mentorship = mentorshipRepository.findByMenteeId(menteeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship assignment not found for student ID: " + menteeId));
        mentorshipRepository.delete(mentorship);
    }

    public List<MentorshipDto> getMenteesForFacultyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Faculty faculty = facultyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user: " + email));
        return getMenteesForFaculty(faculty.getId());
    }

    public List<MentorshipDto> getMenteesForFaculty(Long facultyId) {
        return mentorshipRepository.findByMentorId(facultyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void verifyMentorMenteeRelationship(String facultyEmail, Long studentId) {
        User user = userRepository.findByEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + facultyEmail));
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        Faculty faculty = facultyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user: " + facultyEmail));
        Mentorship mentorship = mentorshipRepository.findByMenteeId(studentId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Access denied: No mentorship assignment exists for this student."));
        if (!mentorship.getMentor().getId().equals(faculty.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You are not the assigned mentor for this student.");
        }
    }

    @Transactional
    public MentorNoteDto createNote(MentorNoteDto dto, String mentorEmail) {
        User user = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + mentorEmail));
        Faculty faculty = facultyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found: " + mentorEmail));
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + dto.getStudentId()));

        if (dto.getNote() == null || dto.getNote().trim().isEmpty()) {
            throw new BadRequestException("Counseling note text is required");
        }

        MentorNote mentorNote = MentorNote.builder()
                .mentor(faculty)
                .student(student)
                .note(dto.getNote().trim())
                .category(dto.getCategory() != null ? dto.getCategory().toUpperCase() : "ACADEMIC")
                .build();

        return mapToNoteDto(mentorNoteRepository.save(mentorNote));
    }

    public List<MentorNoteDto> getNotesForStudent(Long studentId) {
        return mentorNoteRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::mapToNoteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MentorNoteDto updateNote(Long noteId, String noteText, String category, String mentorEmail) {
        MentorNote note = mentorNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor note not found with ID: " + noteId));

        if (noteText != null && !noteText.trim().isEmpty()) {
            note.setNote(noteText.trim());
        }
        if (category != null) {
            note.setCategory(category.toUpperCase());
        }

        return mapToNoteDto(mentorNoteRepository.save(note));
    }

    @Transactional
    public void deleteNote(Long noteId, String mentorEmail) {
        MentorNote note = mentorNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor note not found with ID: " + noteId));
        mentorNoteRepository.delete(note);
    }

    private MentorshipDto mapToDto(Mentorship m) {
        return MentorshipDto.builder()
                .id(m.getId())
                .mentorId(m.getMentor().getId())
                .mentorName(m.getMentor().getUser().getName())
                .mentorEmployeeId(m.getMentor().getEmployeeId())
                .menteeId(m.getMentee().getId())
                .menteeName(m.getMentee().getUser().getName())
                .menteeRollNumber(m.getMentee().getRollNumber())
                .menteeDepartmentName(m.getMentee().getDepartment() != null ? m.getMentee().getDepartment().getName() : "N/A")
                .menteeSemester(m.getMentee().getSemester())
                .assignedAt(m.getAssignedAt())
                .build();
    }

    private MentorNoteDto mapToNoteDto(MentorNote n) {
        return MentorNoteDto.builder()
                .id(n.getId())
                .mentorId(n.getMentor().getId())
                .mentorName(n.getMentor().getUser().getName())
                .studentId(n.getStudent().getId())
                .studentName(n.getStudent().getUser().getName())
                .note(n.getNote())
                .category(n.getCategory())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
