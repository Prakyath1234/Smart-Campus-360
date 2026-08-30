package com.smartcampus.service;

import com.smartcampus.dto.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class AtsAnalysisServiceTest {

    @Autowired
    private AtsAnalysisService atsAnalysisService;

    @Test
    void testPerfectSkillMatch() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("I have Java, Spring Boot, MySQL, and Docker experience.")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Required:\nJava\nSpring Boot\nMySQL\nDocker")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        assertEquals(40, result.getSkillMatch().getScore());
        assertTrue(result.getSkillMatch().getMissingRequiredSkills().isEmpty());
    }

    @Test
    void testPartialSkillMatchAndScoreDeduction() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("I have Java, Spring Boot, and MySQL experience.")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Required:\nJava\nSpring Boot\nMySQL\nDocker")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // Required matched = 3/4. Score = 40 * (3/4 * 0.70 + 1.0 * 0.30) if preferred is empty? Wait, if preferred is empty, pref ratio is 1.0.
        // reqRatio = 3/4 = 0.75. prefRatio = 1.0. Weighted = 0.75 * 0.70 + 1.0 * 0.30 = 0.525 + 0.30 = 0.825.
        // Score = 40 * 0.825 = 33.
        assertEquals(33, result.getSkillMatch().getScore());
        assertTrue(result.getSkillMatch().getMissingRequiredSkills().contains("Docker"));
    }

    @Test
    void testNoSkillMatch() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("I have Python and Flask experience.")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Required:\nJava\nSpring Boot")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // reqRatio = 0.0. prefRatio = 1.0. Score = 40 * (0.0 * 0.70 + 1.0 * 0.30) = 40 * 0.30 = 12.
        assertEquals(12, result.getSkillMatch().getScore());
    }

    @Test
    void testJavaVsJavaScriptCollision() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("I have JavaScript experience but no other languages.")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Required:\nJava")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // Java should not match JavaScript
        assertTrue(result.getSkillMatch().getMissingRequiredSkills().contains("Java"));
    }

    @Test
    void testCVsCppCollision() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("I have C++ experience.")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Required:\nC")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // C should not match C++
        assertTrue(result.getSkillMatch().getMissingRequiredSkills().contains("C"));
    }

    @Test
    void testReactVsReactJSNormalization() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("I have ReactJS experience.")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Required:\nReact")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // ReactJS should normalize and match React
        assertTrue(result.getSkillMatch().getMatchedRequiredSkills().contains("React"));
    }

    @Test
    void testPostgresVsPostgreSQLNormalization() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("I have Postgres experience.")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Required:\nPostgreSQL")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        assertTrue(result.getSkillMatch().getMatchedRequiredSkills().contains("PostgreSQL"));
    }

    @Test
    void testExperienceMatch() {
        ParsedResume resume = ParsedResume.builder()
                .experience(Arrays.asList(
                        ExperienceEntry.builder().startDate("2020").endDate("2024").current(false).build(),
                        ExperienceEntry.builder().startDate("2024").current(true).build()
                ))
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Requires 5+ years of experience in Java.")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // 2020-2024 = 4 years. 2024-Current(2026) = 2 years. Total = 6 years.
        // Required = 5 years. Matches or exceeds.
        assertEquals(25, result.getExperienceMatch().getScore());
        assertEquals("HIGH", result.getExperienceMatch().getConfidence());
    }

    @Test
    void testExperienceMismatch() {
        ParsedResume resume = ParsedResume.builder()
                .experience(Collections.singletonList(
                        ExperienceEntry.builder().startDate("2024").current(true).build()
                ))
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Requires 5+ years of experience in Java.")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // Detected = 2 years. Required = 5 years.
        // Score = 25 * (2/5) = 10.
        assertEquals(10, result.getExperienceMatch().getScore());
    }

    @Test
    void testEducationMatch() {
        ParsedResume resume = ParsedResume.builder()
                .education(Collections.singletonList(
                        EducationEntry.builder().degree("Master of Technology in Computer Science").build()
                ))
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Minimum required: Bachelor's degree in Computer Science.")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        assertEquals(10, result.getEducationMatch().getScore());
    }

    @Test
    void testEducationMismatch() {
        ParsedResume resume = ParsedResume.builder()
                .education(Collections.singletonList(
                        EducationEntry.builder().degree("High School Diploma").build()
                ))
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Minimum required: PhD in Computer Science.")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        assertEquals(5, result.getEducationMatch().getScore());
    }

    @Test
    void testKeywordCoverageAndNoInflatedScores() {
        ParsedResume resume = ParsedResume.builder()
                .rawText("Java Java Java Java Java")
                .build();

        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription("Java developer position. Requires Java and JVM knowledge.")
                .build();

        AtsAnalysisResult result = atsAnalysisService.analyze(request);
        // Repeated Java keyword should not artificially inflate the score past max unique keywords.
        assertTrue(result.getKeywordCoverage() <= 15);
    }

    @Test
    void testOversizedJobDescription() {
        StringBuilder jd = new StringBuilder();
        for (int i = 0; i < 11000; i++) {
            jd.append("a");
        }
        ParsedResume resume = ParsedResume.builder().build();
        AtsAnalysisRequest request = AtsAnalysisRequest.builder()
                .resume(resume)
                .jobDescription(jd.toString())
                .build();

        assertThrows(BadRequestException.class, () -> atsAnalysisService.analyze(request));
    }
}
