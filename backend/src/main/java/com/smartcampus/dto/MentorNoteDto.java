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
public class MentorNoteDto {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private Long studentId;
    private String studentName;
    private String note;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
