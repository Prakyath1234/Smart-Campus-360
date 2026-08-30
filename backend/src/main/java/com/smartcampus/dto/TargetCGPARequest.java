package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetCGPARequest {
    private BigDecimal currentCGPA;
    private Integer completedCredits;
    private Integer futureCredits;
    private BigDecimal targetCGPA;
}
