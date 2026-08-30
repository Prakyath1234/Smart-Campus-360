package com.smartcampus.service;

import com.smartcampus.dto.ServiceRequestDto;
import com.smartcampus.entity.ServiceRequest;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.ServiceRequestRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ServiceRequestService(ServiceRequestRepository serviceRequestRepository, StudentRepository studentRepository,
                                 UserRepository userRepository, NotificationService notificationService) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ServiceRequestDto createRequest(ServiceRequestDto dto, String studentEmail) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + studentEmail));
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found: " + studentEmail));

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Request title is required");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Request details are required");
        }

        String type = dto.getRequestType() != null ? dto.getRequestType().toUpperCase() : "OTHER";

        ServiceRequest request = ServiceRequest.builder()
                .student(student)
                .requestType(type)
                .title(dto.getTitle().trim())
                .description(dto.getDescription().trim())
                .status("PENDING")
                .build();

        return mapToDto(serviceRequestRepository.save(request));
    }

    public List<ServiceRequestDto> getStudentRequests(String studentEmail) {
        User user = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + studentEmail));
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found: " + studentEmail));

        return serviceRequestRepository.findByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Page<ServiceRequestDto> getAdminRequests(String search, String requestType, String status, Pageable pageable) {
        Specification<ServiceRequest> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<ServiceRequest, Student> studentJoin = root.join("student");
            Join<Student, User> userJoin = studentJoin.join("user");

            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate studentMatch = cb.like(cb.lower(userJoin.get("name")), pattern);
                Predicate rollMatch = cb.like(cb.lower(studentJoin.get("rollNumber")), pattern);
                predicates.add(cb.or(titleMatch, studentMatch, rollMatch));
            }

            if (requestType != null && !requestType.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("requestType")), requestType.trim().toUpperCase()));
            }

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), status.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return serviceRequestRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    @Transactional
    public ServiceRequestDto updateStatus(Long id, String status, String comments) {
        ServiceRequest request = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found with ID: " + id));

        request.setStatus(status.toUpperCase());
        if (comments != null) {
            request.setAdminComments(comments);
        }

        request = serviceRequestRepository.save(request);

        // Notify student
        notificationService.createNotification(request.getStudent().getUser(), "Service Request Update",
                "Your request '" + request.getTitle() + "' is now " + status + ". Notes: " + (comments != null ? comments : "N/A"),
                com.smartcampus.entity.NotificationType.SYSTEM);

        return mapToDto(request);
    }

    private ServiceRequestDto mapToDto(ServiceRequest r) {
        return ServiceRequestDto.builder()
                .id(r.getId())
                .studentId(r.getStudent().getId())
                .studentName(r.getStudent().getUser().getName())
                .rollNumber(r.getStudent().getRollNumber())
                .departmentName(r.getStudent().getDepartment() != null ? r.getStudent().getDepartment().getName() : "N/A")
                .requestType(r.getRequestType())
                .title(r.getTitle())
                .description(r.getDescription())
                .status(r.getStatus())
                .adminComments(r.getAdminComments())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
