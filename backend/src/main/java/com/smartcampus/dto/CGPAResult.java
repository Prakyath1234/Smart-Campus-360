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
public class CGPAResult {
    private BigDecimal cgpa;
    private Integer totalCredits;
    private List<CGPACalculationRequest.SemesterScore> semesters;
}
