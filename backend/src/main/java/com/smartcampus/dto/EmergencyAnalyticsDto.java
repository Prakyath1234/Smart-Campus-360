package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAnalyticsDto {
    private Long totalEmergencies;
    private Long resolvedCount;
    private Long cancelledCount;
    private Double averageResponseTimeMinutes;
    private Map<String, Long> typeDistribution;
    private Map<String, Long> statusDistribution;
}
