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
public class MentorshipDto {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private String mentorEmployeeId;
    private Long menteeId;
    private String menteeName;
    private String menteeRollNumber;
    private String menteeDepartmentName;
    private Integer menteeSemester;
    private LocalDateTime assignedAt;
}
