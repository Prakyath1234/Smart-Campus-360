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
public class ServiceRequestDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String departmentName;
    private String requestType;
    private String title;
    private String description;
    private String status;
    private String adminComments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
