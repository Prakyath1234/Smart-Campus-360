package com.smartcampus.smarttools.controller;

import com.smartcampus.smarttools.model.ParsedResume;
import com.smartcampus.smarttools.service.ResumeParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeParserController {

    private final ResumeParserService resumeParserService;

    public ResumeParserController(ResumeParserService resumeParserService) {
        this.resumeParserService = resumeParserService;
    }

    @PostMapping(value = "/parse", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> parseResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "includeRawText", defaultValue = "false") boolean includeRawText
    ) {
        ParsedResume parsedResume = resumeParserService.parseResume(file, includeRawText);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", parsedResume);

        return ResponseEntity.ok(response);
    }
}
