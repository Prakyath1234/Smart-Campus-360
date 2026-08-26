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
public class ComplaintDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolutionComments;
}
