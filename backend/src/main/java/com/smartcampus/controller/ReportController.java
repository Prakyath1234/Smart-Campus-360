package com.smartcampus.controller;

import com.smartcampus.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_FACULTY')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> downloadCsvReport(@RequestParam(defaultValue = "students") String type) {
        String csvContent = reportService.generateCsvReport(type);
        byte[] bytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String filename = type.toLowerCase() + "_report.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
