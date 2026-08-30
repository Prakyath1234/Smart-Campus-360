package com.smartcampus.dto;

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
public class AtsAnalysisResult {
    private int overallScore;
    private Map<String, Integer> breakdown; // e.g. "skills", "experience", "education", "keywords", "projects"
    private SkillMatchResult skillMatch;
    private ExperienceMatchResult experienceMatch;
    private EducationMatchResult educationMatch;
    private int keywordCoverage;
    private int projectRelevance;
    private List<Recommendation> recommendations;
}
