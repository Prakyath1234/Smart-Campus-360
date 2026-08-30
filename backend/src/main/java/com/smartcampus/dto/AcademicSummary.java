package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicSummary {
    private BigDecimal currentSGPA;
    private BigDecimal currentCGPA;
    private Integer totalCredits;
    private List<SemesterHistoryEntry> semesterHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterHistoryEntry {
        private Integer semester;
        private BigDecimal sgpa;
        private Integer credits;
    }
}
