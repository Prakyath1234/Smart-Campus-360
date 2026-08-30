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
public class SGPAResult {
    private BigDecimal sgpa;
    private Integer totalCredits;
    private Integer totalCreditPoints;
    private List<SGPACalculationRequest.SubjectScore> subjects;
}
