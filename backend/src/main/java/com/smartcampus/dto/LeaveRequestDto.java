package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String departmentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private Long reviewedByFacultyId;
    private String reviewedByFacultyName;
    private String reviewComments;
    private LocalDateTime createdAt;
}
