package com.smartcampus.controller;

import com.smartcampus.dto.*;
import com.smartcampus.entity.Student;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.service.AcademicCalculationService;
import com.smartcampus.service.AcademicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AcademicCalculatorController {

    private final AcademicCalculationService academicCalculationService;
    private final AcademicService academicService;

    public AcademicCalculatorController(AcademicCalculationService academicCalculationService,
                                        AcademicService academicService) {
        this.academicCalculationService = academicCalculationService;
        this.academicService = academicService;
    }

    @PostMapping("/academic/calculator/sgpa")
    public ResponseEntity<SGPAResult> calculateSGPA(@RequestBody SGPACalculationRequest request) {
        return ResponseEntity.ok(academicCalculationService.calculateSGPA(request));
    }

    @PostMapping("/academic/calculator/cgpa")
    public ResponseEntity<CGPAResult> calculateCGPA(@RequestBody CGPACalculationRequest request) {
        return ResponseEntity.ok(academicCalculationService.calculateCGPA(request));
    }

    @PostMapping("/academic/calculator/what-if")
    public ResponseEntity<WhatIfResult> calculateWhatIf(@RequestBody WhatIfRequest request) {
        return ResponseEntity.ok(academicCalculationService.calculateWhatIfSGPA(request));
    }

    @PostMapping("/academic/calculator/target-cgpa")
    public ResponseEntity<TargetCGPAResult> calculateTargetCGPA(@RequestBody TargetCGPARequest request) {
        return ResponseEntity.ok(academicCalculationService.calculateRequiredSGPA(request));
    }

    @GetMapping("/student/academic/summary")
    public ResponseEntity<AcademicSummary> getAcademicSummary(Principal principal) {
        if (principal == null) {
            throw new BadRequestException("Unauthenticated request");
        }
        Student student = academicService.getStudentEntityByEmail(principal.getName());
        return ResponseEntity.ok(academicCalculationService.getStudentAcademicSummary(student));
    }
}
