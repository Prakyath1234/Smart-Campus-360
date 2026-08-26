package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimetableDto {
    private Long id;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String facultyName;
    private String classroom;
    private Integer semester;
}
