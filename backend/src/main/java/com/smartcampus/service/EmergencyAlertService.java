package com.smartcampus.service;

import com.smartcampus.dto.EmergencyAlertDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.EmergencyAlertRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmergencyAlertService {

    private final EmergencyAlertRepository emergencyAlertRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EmergencyAlertService(EmergencyAlertRepository emergencyAlertRepository, StudentRepository studentRepository,
                                 UserRepository userRepository, NotificationService notificationService) {
        this.emergencyAlertRepository = emergencyAlertRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public EmergencyAlertDto triggerSos(EmergencyAlertDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        EmergencyAlert alert = EmergencyAlert.builder()
                .student(student)
                .emergencyType(dto.getEmergencyType())
                .description(dto.getDescription())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .locationText(dto.getLocationText())
                .status(EmergencyStatus.ACTIVE)
                .build();

        alert = emergencyAlertRepository.save(alert);

        // Notify all Admin & Security users
        List<User> operators = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.SECURITY || u.getRole() == Role.ADMIN)
                .toList();

        String alertMessage = String.format("🚨 SOS: %s reported by student %s (%s). Location: %s",
                alert.getEmergencyType(), student.getUser().getName(), student.getRollNumber(),
                alert.getLocationText() != null ? alert.getLocationText() : "Coordinates: " + alert.getLatitude() + ", " + alert.getLongitude());

        for (User operator : operators) {
            notificationService.createNotification(operator, "🚨 ACTIVE EMERGENCY SOS", alertMessage, NotificationType.SOS);
        }

        return mapToDto(alert);
    }

    public List<EmergencyAlertDto> getActiveAlerts() {
        return emergencyAlertRepository.findByStatus(EmergencyStatus.ACTIVE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EmergencyAlertDto> getAllAlerts() {
        return emergencyAlertRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EmergencyAlertDto getAlertById(Long id) {
        EmergencyAlert alert = emergencyAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency incident not found"));
        return mapToDto(alert);
    }

    @Transactional
    public EmergencyAlertDto updateAlertStatus(Long id, String statusString, Long resolverUserId) {
        EmergencyAlert alert = emergencyAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency incident not found"));

        EmergencyStatus status;
        try {
            status = EmergencyStatus.valueOf(statusString.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid emergency status");
        }

        alert.setStatus(status);

        if (status == EmergencyStatus.ACKNOWLEDGED) {
            alert.setAcknowledgedAt(LocalDateTime.now());
        } else if (status == EmergencyStatus.RESOLVED) {
            alert.setResolvedAt(LocalDateTime.now());
            if (resolverUserId != null) {
                User resolver = userRepository.findById(resolverUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("Responder user not found"));
                alert.setResolvedBy(resolver);
            }
        }

        alert = emergencyAlertRepository.save(alert);
        return mapToDto(alert);
    }

    private EmergencyAlertDto mapToDto(EmergencyAlert alert) {
        return EmergencyAlertDto.builder()
                .id(alert.getId())
                .studentId(alert.getStudent().getId())
                .studentName(alert.getStudent().getUser().getName())
                .rollNumber(alert.getStudent().getRollNumber())
                .phone(alert.getStudent().getUser().getPhone())
                .emergencyType(alert.getEmergencyType())
                .description(alert.getDescription())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .locationText(alert.getLocationText())
                .status(alert.getStatus().name())
                .createdAt(alert.getCreatedAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .resolvedAt(alert.getResolvedAt())
                .resolvedByUserId(alert.getResolvedBy() != null ? alert.getResolvedBy().getId() : null)
                .resolvedByUserName(alert.getResolvedBy() != null ? alert.getResolvedBy().getName() : null)
                .build();
    }
}
