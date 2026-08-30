package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPerformanceDto {
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private Double attendancePercentage;
    private Long totalClasses;
    private Long presentClasses;
    private Double averageMarks;
    private String bestSubject;
    private String weakestSubject;
    private String riskStatus; // AT_RISK or GOOD
    private String riskReason;
    private List<SubjectScore> subjectScores;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectScore {
        private String subjectName;
        private String subjectCode;
        private Double totalMarks;
        private String grade;
        private Double attendancePercentage;
    }
}
