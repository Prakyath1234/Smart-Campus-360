package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlertDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String phone;
    private String emergencyType;
    private String description;
    private Double latitude;
    private Double longitude;
    private String locationText;
    private String status; // ACTIVE, ACKNOWLEDGED, IN_PROGRESS, RESOLVED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private Long resolvedByUserId;
    private String resolvedByUserName;
}
