package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceMatchResult {
    private int score;
    private String requiredYears; // e.g. "3+" or "UNKNOWN"
    private Double detectedYears;
    private String explanation;
    private String confidence;
}
