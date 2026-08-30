package com.smartcampus.smarttools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedResume {
    private ResumeProfile profile;
    private Map<String, List<String>> skills;
    private List<EducationEntry> education;
    private List<ExperienceEntry> experience;
    private List<ProjectEntry> projects;
    private List<CertificationEntry> certifications;
    private List<String> achievements;
    private Map<String, String> confidence;
    private String rawText;
}
