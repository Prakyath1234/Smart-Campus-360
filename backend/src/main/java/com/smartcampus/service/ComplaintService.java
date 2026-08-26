package com.smartcampus.service;

import com.smartcampus.dto.ComplaintDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.ComplaintRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AiClassificationService aiClassificationService;
    private final NotificationService notificationService;

    public ComplaintService(ComplaintRepository complaintRepository, StudentRepository studentRepository,
                            UserRepository userRepository, AiClassificationService aiClassificationService,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.aiClassificationService = aiClassificationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ComplaintDto submitComplaint(ComplaintDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        // Use AI helper to determine category and priority
        AiClassificationService.ClassificationResult aiResult = aiClassificationService.classify(dto.getDescription());

        Complaint complaint = Complaint.builder()
                .student(student)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .category(aiResult.getCategory())
                .priority(aiResult.getPriority())
                .status(ComplaintStatus.OPEN)
                .build();

        complaint = complaintRepository.save(complaint);

        // Notify admins of new complaint
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .toList();
        for (User admin : admins) {
            notificationService.createNotification(admin, "New Complaint Lodged",
                    "A new " + complaint.getCategory() + " (" + complaint.getPriority() + ") complaint has been lodged: " + complaint.getTitle(),
                    NotificationType.COMPLAINT);
        }

        return mapToDto(complaint);
    }

    public List<ComplaintDto> getStudentComplaints(Long studentId) {
        return complaintRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<ComplaintDto> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintDto updateComplaintStatus(Long complaintId, String statusString, String comments) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        ComplaintStatus status = ComplaintStatus.valueOf(statusString.toUpperCase());
        complaint.setStatus(status);
        
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) {
            complaint.setResolvedAt(LocalDateTime.now());
            complaint.setResolutionComments(comments);
        }

        complaint = complaintRepository.save(complaint);

        // Notify student
        notificationService.createNotification(complaint.getStudent().getUser(), "Complaint Status Update",
                "Your complaint '" + complaint.getTitle() + "' is now " + status.name() + ". Notes: " + comments,
                NotificationType.COMPLAINT);

        return mapToDto(complaint);
    }

    private ComplaintDto mapToDto(Complaint complaint) {
        return ComplaintDto.builder()
                .id(complaint.getId())
                .studentId(complaint.getStudent().getId())
                .studentName(complaint.getStudent().getUser().getName())
                .rollNumber(complaint.getStudent().getRollNumber())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory().name())
                .priority(complaint.getPriority().name())
                .status(complaint.getStatus().name())
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .resolutionComments(complaint.getResolutionComments())
                .build();
    }
}
