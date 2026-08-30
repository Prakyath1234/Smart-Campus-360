package com.smartcampus.service;

import com.smartcampus.dto.*;
import com.smartcampus.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class AcademicCalculationServiceTest {

    @Autowired
    private AcademicCalculationService academicCalculationService;

    @Test
    void testCalculateSGPASuccess() {
        SGPACalculationRequest request = SGPACalculationRequest.builder()
                .subjects(Arrays.asList(
                        SGPACalculationRequest.SubjectScore.builder().subjectName("Sub1").credits(4).grade("A+").build(), // 10 * 4 = 40
                        SGPACalculationRequest.SubjectScore.builder().subjectName("Sub2").credits(3).grade("B").build()    // 7 * 3 = 21
                ))
                .build();

        // Total credits = 7, Total points = 61. 61 / 7 = 8.714 -> 8.71
        SGPAResult result = academicCalculationService.calculateSGPA(request);
        assertEquals(new BigDecimal("8.71"), result.getSgpa());
        assertEquals(7, result.getTotalCredits());
    }

    @Test
    void testCalculateSGPAWeightedRoundingHalfUp() {
        SGPACalculationRequest request = SGPACalculationRequest.builder()
                .subjects(Arrays.asList(
                        SGPACalculationRequest.SubjectScore.builder().subjectName("Sub1").credits(3).grade("B+").build(), // 8 * 3 = 24
                        SGPACalculationRequest.SubjectScore.builder().subjectName("Sub2").credits(4).grade("A").build(),  // 9 * 4 = 36
                        SGPACalculationRequest.SubjectScore.builder().subjectName("Sub3").credits(1).grade("F").build()   // 0 * 1 = 0
                ))
                .build();

        // Total credits = 8, Total points = 60. 60 / 8 = 7.50
        SGPAResult result = academicCalculationService.calculateSGPA(request);
        assertEquals(new BigDecimal("7.50"), result.getSgpa());
    }

    @Test
    void testCalculateSGPAInvalidGrade() {
        SGPACalculationRequest request = SGPACalculationRequest.builder()
                .subjects(Collections.singletonList(
                        SGPACalculationRequest.SubjectScore.builder().subjectName("Sub1").credits(3).grade("INVALID").build()
                ))
                .build();

        assertThrows(BadRequestException.class, () -> academicCalculationService.calculateSGPA(request));
    }

    @Test
    void testCalculateSGPAZeroCredits() {
        SGPACalculationRequest request = SGPACalculationRequest.builder()
                .subjects(Collections.singletonList(
                        SGPACalculationRequest.SubjectScore.builder().subjectName("Sub1").credits(0).grade("A").build()
                ))
                .build();

        assertThrows(BadRequestException.class, () -> academicCalculationService.calculateSGPA(request));
    }

    @Test
    void testCalculateCGPASuccess() {
        CGPACalculationRequest request = CGPACalculationRequest.builder()
                .semesters(Arrays.asList(
                        CGPACalculationRequest.SemesterScore.builder().semester(1).sgpa(new BigDecimal("8.20")).credits(24).build(),
                        CGPACalculationRequest.SemesterScore.builder().semester(2).sgpa(new BigDecimal("8.60")).credits(24).build()
                ))
                .build();

        // (8.20 * 24 + 8.60 * 24) / 48 = 8.40
        CGPAResult result = academicCalculationService.calculateCGPA(request);
        assertEquals(new BigDecimal("8.40"), result.getCgpa());
        assertEquals(48, result.getTotalCredits());
    }

    @Test
    void testCalculateCGPAWeightedCredits() {
        CGPACalculationRequest request = CGPACalculationRequest.builder()
                .semesters(Arrays.asList(
                        CGPACalculationRequest.SemesterScore.builder().semester(1).sgpa(new BigDecimal("7.50")).credits(20).build(),
                        CGPACalculationRequest.SemesterScore.builder().semester(2).sgpa(new BigDecimal("8.80")).credits(24).build()
                ))
                .build();

        // (7.5 * 20 + 8.8 * 24) / 44 = (150 + 211.2) / 44 = 361.2 / 44 = 8.209 -> 8.21
        CGPAResult result = academicCalculationService.calculateCGPA(request);
        assertEquals(new BigDecimal("8.21"), result.getCgpa());
    }

    @Test
    void testWhatIfSGPACalculation() {
        WhatIfRequest request = WhatIfRequest.builder()
                .subjects(Arrays.asList(
                        WhatIfRequest.WhatIfSubject.builder().subjectName("Sub1").credits(4).currentGrade("B").projectedGrade("A").build(),
                        WhatIfRequest.WhatIfSubject.builder().subjectName("Sub2").credits(3).currentGrade("A").projectedGrade("A").build()
                ))
                .build();

        WhatIfResult result = academicCalculationService.calculateWhatIfSGPA(request);
        // Current: B (7)*4 + A (9)*3 = 28 + 27 = 55. 55 / 7 = 7.86
        // Projected: A (9)*4 + A (9)*3 = 36 + 27 = 63. 63 / 7 = 9.00
        // Diff = 9.00 - 7.86 = 1.14
        assertEquals(new BigDecimal("7.86"), result.getCurrentSGPA());
        assertEquals(new BigDecimal("9.00"), result.getProjectedSGPA());
        assertEquals(new BigDecimal("1.14"), result.getDifference());
    }

    @Test
    void testTargetCGPAPossible() {
        TargetCGPARequest request = TargetCGPARequest.builder()
                .currentCGPA(new BigDecimal("7.50"))
                .completedCredits(100)
                .futureCredits(24)
                .targetCGPA(new BigDecimal("8.00"))
                .build();

        TargetCGPAResult result = academicCalculationService.calculateRequiredSGPA(request);
        assertFalse(result.isPossible());
    }

    @Test
    void testTargetCGPAAchievable() {
        TargetCGPARequest request = TargetCGPARequest.builder()
                .currentCGPA(new BigDecimal("7.50"))
                .completedCredits(100)
                .futureCredits(24)
                .targetCGPA(new BigDecimal("7.80"))
                .build();

        TargetCGPAResult result = academicCalculationService.calculateRequiredSGPA(request);
        assertTrue(result.isPossible());
        // Required points = 7.8 * 124 - 7.5 * 100 = 967.2 - 750 = 217.2
        // Required SGPA = 217.2 / 24 = 9.05
        assertEquals(new BigDecimal("9.05"), result.getRequiredFutureSGPA());
    }
}
