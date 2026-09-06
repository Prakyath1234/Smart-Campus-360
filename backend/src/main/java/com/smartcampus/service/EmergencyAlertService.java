package com.smartcampus.service;

import com.smartcampus.dto.EmergencyAlertDto;
import com.smartcampus.dto.EmergencyAnalyticsDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.EmergencyAlertRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmergencyAlertService {

    private final EmergencyAlertRepository emergencyAlertRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final StatusHistoryService statusHistoryService;

    public EmergencyAlertService(EmergencyAlertRepository emergencyAlertRepository, StudentRepository studentRepository,
                                 UserRepository userRepository, NotificationService notificationService,
                                 AuditLogService auditLogService, StatusHistoryService statusHistoryService) {
        this.emergencyAlertRepository = emergencyAlertRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.statusHistoryService = statusHistoryService;
    }

    @Transactional
    public EmergencyAlertDto triggerSos(EmergencyAlertDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return processSos(dto, student);
    }

    @Transactional
    public EmergencyAlertDto triggerSosForEmail(EmergencyAlertDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Student student = studentRepository.findByUserId(user.getId())
                .orElseGet(() -> studentRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No campus profile found for emergency dispatch")));

        return processSos(dto, student);
    }

    private EmergencyAlertDto processSos(EmergencyAlertDto dto, Student student) {
        String emergencyType = (dto.getEmergencyType() != null && !dto.getEmergencyType().trim().isEmpty())
                ? dto.getEmergencyType() : "MEDICAL";

        EmergencyAlert alert = EmergencyAlert.builder()
                .student(student)
                .emergencyType(emergencyType)
                .description(dto.getDescription() != null ? dto.getDescription() : "Emergency alert triggered")
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .locationText(dto.getLocationText() != null ? dto.getLocationText() : "Campus Grounds")
                .status(EmergencyStatus.ACTIVE)
                .build();

        alert = emergencyAlertRepository.save(alert);

        // Record Audit Log and Status History
        auditLogService.logAction(student.getUser().getEmail(), student.getUser().getRole().name(),
                "SOS_TRIGGERED", "EmergencyAlert", alert.getId(),
                "Distress signal triggered (" + emergencyType + ") at " + alert.getLocationText(), null);

        statusHistoryService.recordStatusChange("EmergencyAlert", alert.getId(), null, "ACTIVE",
                student.getUser().getEmail(), "SOS Alert Triggered");

        List<User> operators = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.SECURITY || u.getRole() == Role.ADMIN)
                .toList();

        String locationInfo = (alert.getLatitude() != null && alert.getLongitude() != null)
                ? String.format("Coordinates: %.4f, %.4f (%s)", alert.getLatitude(), alert.getLongitude(), alert.getLocationText())
                : alert.getLocationText();

        String alertMessage = String.format("🚨 SOS: %s reported by student %s (%s). Location: %s",
                alert.getEmergencyType(), student.getUser().getName(), student.getRollNumber(), locationInfo);

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

    public EmergencyAnalyticsDto getEmergencyAnalytics() {
        List<EmergencyAlert> alerts = emergencyAlertRepository.findAll();
        long total = alerts.size();
        long resolved = alerts.stream().filter(a -> a.getStatus() == EmergencyStatus.RESOLVED).count();
        long cancelled = alerts.stream().filter(a -> a.getStatus() == EmergencyStatus.CANCELLED).count();

        Map<String, Long> typeDist = new HashMap<>();
        Map<String, Long> statusDist = new HashMap<>();

        for (EmergencyAlert a : alerts) {
            typeDist.put(a.getEmergencyType(), typeDist.getOrDefault(a.getEmergencyType(), 0L) + 1);
            statusDist.put(a.getStatus().name(), statusDist.getOrDefault(a.getStatus().name(), 0L) + 1);
        }

        List<Duration> durations = alerts.stream()
                .filter(a -> a.getResolvedAt() != null || a.getAcknowledgedAt() != null)
                .map(a -> {
                    LocalDateTime target = a.getAcknowledgedAt() != null ? a.getAcknowledgedAt() : a.getResolvedAt();
                    return Duration.between(a.getCreatedAt(), target);
                })
                .toList();

        double avgMinutes = durations.isEmpty() ? 0.0 :
                durations.stream().mapToLong(Duration::toMinutes).average().orElse(0.0);

        return EmergencyAnalyticsDto.builder()
                .totalEmergencies(total)
                .resolvedCount(resolved)
                .cancelledCount(cancelled)
                .averageResponseTimeMinutes(Math.round(avgMinutes * 10.0) / 10.0)
                .typeDistribution(typeDist)
                .statusDistribution(statusDist)
                .build();
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

        EmergencyStatus newStatus;
        try {
            newStatus = EmergencyStatus.valueOf(statusString.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid emergency status: " + statusString);
        }

        EmergencyStatus currentStatus = alert.getStatus();

        // Enforce State Machine Transitions
        if (currentStatus == EmergencyStatus.RESOLVED || currentStatus == EmergencyStatus.CANCELLED) {
            throw new BadRequestException("Cannot change status of an already " + currentStatus + " emergency alert.");
        }

        if (currentStatus == EmergencyStatus.ACTIVE) {
            if (newStatus != EmergencyStatus.ACKNOWLEDGED && newStatus != EmergencyStatus.CANCELLED) {
                throw new BadRequestException("Active emergency can only be transitioned to ACKNOWLEDGED or CANCELLED.");
            }
        } else if (currentStatus == EmergencyStatus.ACKNOWLEDGED) {
            if (newStatus != EmergencyStatus.IN_PROGRESS && newStatus != EmergencyStatus.CANCELLED && newStatus != EmergencyStatus.RESOLVED) {
                throw new BadRequestException("Acknowledged emergency can only be transitioned to IN_PROGRESS, RESOLVED, or CANCELLED.");
            }
        } else if (currentStatus == EmergencyStatus.IN_PROGRESS) {
            if (newStatus != EmergencyStatus.RESOLVED && newStatus != EmergencyStatus.CANCELLED) {
                throw new BadRequestException("In-progress emergency can only be transitioned to RESOLVED or CANCELLED.");
            }
        }

        alert.setStatus(newStatus);

        if (newStatus == EmergencyStatus.ACKNOWLEDGED) {
            alert.setAcknowledgedAt(LocalDateTime.now());
        } else if (newStatus == EmergencyStatus.RESOLVED) {
            alert.setResolvedAt(LocalDateTime.now());
            if (resolverUserId != null) {
                User resolver = userRepository.findById(resolverUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("Responder user not found"));
                alert.setResolvedBy(resolver);
            }
        }

        alert = emergencyAlertRepository.save(alert);

        // Audit Log and Status History
        auditLogService.logAction("SECURITY", "SECURITY", "SOS_" + newStatus.name(),
                "EmergencyAlert", alert.getId(), "Emergency alert status updated from " + currentStatus + " to " + newStatus, null);

        statusHistoryService.recordStatusChange("EmergencyAlert", alert.getId(), currentStatus.name(), newStatus.name(),
                "SECURITY", "Status update to " + newStatus);

        return mapToDto(alert);
    }

    public void verifySosAccess(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.SECURITY) {
            return;
        }
        EmergencyAlert alert = emergencyAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency incident not found: " + id));
        if (!alert.getStudent().getUser().getEmail().equals(email)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied: You cannot view or modify other students' emergency events.");
        }
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
