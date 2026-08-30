package com.smartcampus.controller;

import com.smartcampus.dto.AtsAnalysisRequest;
import com.smartcampus.dto.AtsAnalysisResult;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.service.AtsAnalysisService;
import com.smartcampus.smarttools.model.ParsedResume;
import com.smartcampus.smarttools.service.ResumeParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ats")
@CrossOrigin(origins = "*")
public class AtsAnalysisController {

    private final ResumeParserService resumeParserService;
    private final AtsAnalysisService atsAnalysisService;

    public AtsAnalysisController(ResumeParserService resumeParserService,
                                 AtsAnalysisService atsAnalysisService) {
        this.resumeParserService = resumeParserService;
        this.atsAnalysisService = atsAnalysisService;
    }

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<AtsAnalysisResult> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription
    ) {
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            throw new BadRequestException("Job description cannot be empty.");
        }
        if (jobDescription.length() > 10000) {
            throw new BadRequestException("Job description length exceeds maximum limit of 10000 characters.");
        }

        // Parse resume using the existing service
        ParsedResume parsedResume = resumeParserService.parseResume(file, true);

        // Analyze using AtsAnalysisService
        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(parsedResume)
                .jobDescription(jobDescription)
                .build();

        return ResponseEntity.ok(atsAnalysisService.analyze(request));
    }
}
