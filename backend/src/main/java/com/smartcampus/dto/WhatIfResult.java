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
public class WhatIfResult {
    private BigDecimal currentSGPA;
    private BigDecimal projectedSGPA;
    private BigDecimal difference;
}
