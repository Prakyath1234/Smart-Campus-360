package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationMatchResult {
    private int score;
    private String requiredDegree; // e.g. "Bachelor's" or "UNKNOWN"
    private String detectedDegree;
    private String explanation;
    private String confidence;
}
