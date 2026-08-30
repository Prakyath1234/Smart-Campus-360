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
public class AnnouncementDto {
    private Long id;
    private String title;
    private String content;
    private String targetAudience;
    private Long targetDepartmentId;
    private String targetDepartmentName;
    private Integer targetSemester;
    private String authorName;
    private Boolean published;
    private LocalDateTime createdAt;
}
