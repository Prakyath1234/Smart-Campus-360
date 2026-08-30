package com.smartcampus.service;

import com.smartcampus.dto.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.model.ParsedResume;
import com.smartcampus.smarttools.model.EducationEntry;
import com.smartcampus.smarttools.model.ExperienceEntry;
import com.smartcampus.smarttools.model.ProjectEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AtsAnalysisService {

    private static final Map<String, String> SKILL_NORMALIZATION_MAP = new LinkedHashMap<>();
    static {
        SKILL_NORMALIZATION_MAP.put("springboot", "Spring Boot");
        SKILL_NORMALIZATION_MAP.put("spring boot", "Spring Boot");
        SKILL_NORMALIZATION_MAP.put("js", "JavaScript");
        SKILL_NORMALIZATION_MAP.put("javascript", "JavaScript");
        SKILL_NORMALIZATION_MAP.put("postgres", "PostgreSQL");
        SKILL_NORMALIZATION_MAP.put("postgresql", "PostgreSQL");
        SKILL_NORMALIZATION_MAP.put("reactjs", "React");
        SKILL_NORMALIZATION_MAP.put("react.js", "React");
        SKILL_NORMALIZATION_MAP.put("react", "React");
        SKILL_NORMALIZATION_MAP.put("angularjs", "Angular");
        SKILL_NORMALIZATION_MAP.put("angular.js", "Angular");
        SKILL_NORMALIZATION_MAP.put("angular", "Angular");
        SKILL_NORMALIZATION_MAP.put("nodejs", "Node.js");
        SKILL_NORMALIZATION_MAP.put("node.js", "Node.js");
        SKILL_NORMALIZATION_MAP.put("k8s", "Kubernetes");
        SKILL_NORMALIZATION_MAP.put("kubernetes", "Kubernetes");
    }

    private static final List<String> SKILL_DICTIONARY = Arrays.asList(
            "Java", "Python", "C++", "C", "C#", "Go", "Kotlin", "Swift", "Ruby", "TypeScript", "JavaScript", "js", "Rust",
            "Spring Boot", "springboot", "spring boot", "Spring", "React", "reactjs", "react.js", "reactJS", "Angular", "angularjs", "angular.js", "Vue", "Node.js", "nodejs", "node.js", "Express", "Django", "Flask", "Hibernate",
            "MySQL", "PostgreSQL", "postgres", "postgresql", "MongoDB", "Oracle", "Redis", "SQLite", "Cassandra",
            "AWS", "Azure", "GCP", "Docker", "Kubernetes", "k8s", "Jenkins", "Ansible", "Terraform",
            "Git", "GitHub", "Maven", "Gradle", "JUnit", "Selenium", "Communication", "Leadership", "Teamwork", "Agile", "Scrum"
    );

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "for", "with", "this", "that", "from", "your", "will", "have",
            "are", "our", "their", "about", "using", "work", "team", "development", "developer",
            "role", "experience", "skills", "job", "description", "requirements", "responsibilities",
            "company", "candidate", "position", "working", "preferred", "required", "knowledge",
            "ability", "strong", "written", "verbal", "communication", "years", "degree", "field"
    ));

    public AtsAnalysisResult analyze(AtsAnalysisRequest request) {
        if (request.getJobDescription() == null || request.getJobDescription().trim().isEmpty()) {
            throw new BadRequestException("Job description cannot be empty.");
        }
        if (request.getJobDescription().length() > 10000) {
            throw new BadRequestException("Job description length exceeds maximum limit of 10000 characters.");
        }
        if (request.getResume() == null) {
            throw new BadRequestException("Structured resume data is missing.");
        }

        String jd = request.getJobDescription();
        ParsedResume resume = request.getResume();

        // 1. Extract and Classify Skills from Job Description
        List<String> jdRequired = new ArrayList<>();
        List<String> jdPreferred = new ArrayList<>();
        List<String> jdUnknown = new ArrayList<>();

        parseJdSkills(jd, jdRequired, jdPreferred, jdUnknown);

        // Deduplicate and normalize
        List<String> requiredSkills = normalizeSkills(jdRequired);
        List<String> preferredSkills = normalizeSkills(jdPreferred);
        List<String> unknownSkills = normalizeSkills(jdUnknown);

        // 2. Extract and Normalize Resume Skills
        List<String> resumeSkillsRaw = new ArrayList<>();
        if (resume.getSkills() != null) {
            for (List<String> slist : resume.getSkills().values()) {
                resumeSkillsRaw.addAll(slist);
            }
        }
        if (resume.getRawText() != null) {
            String tempResume = resume.getRawText();
            List<String> sortedDict = new ArrayList<>(SKILL_DICTIONARY);
            sortedDict.sort((a, b) -> Integer.compare(b.length(), a.length()));
            for (String dictSkill : sortedDict) {
                if (containsSkill(tempResume, dictSkill)) {
                    resumeSkillsRaw.add(dictSkill);
                    tempResume = tempResume.replaceAll("(?i)\\b" + Pattern.quote(dictSkill) + "\\b", "___");
                }
            }
        }
        List<String> resumeSkills = normalizeSkills(resumeSkillsRaw);

        // 3. Compute Skill Matches
        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> matchedPreferred = new ArrayList<>();
        List<String> missingPreferred = new ArrayList<>();

        if (unknownSkills.isEmpty()) {
            for (String req : requiredSkills) {
                if (resumeSkills.contains(req)) matchedRequired.add(req);
                else missingRequired.add(req);
            }
            for (String pref : preferredSkills) {
                if (resumeSkills.contains(pref)) matchedPreferred.add(pref);
                else missingPreferred.add(pref);
            }
        } else {
            // Treat all as required if section classification is unknown
            for (String skill : unknownSkills) {
                if (resumeSkills.contains(skill)) matchedRequired.add(skill);
                else missingRequired.add(skill);
            }
        }

        // Skills Score Calculation
        int skillsScore = 40;
        if (!requiredSkills.isEmpty() || !preferredSkills.isEmpty() || !unknownSkills.isEmpty()) {
            if (unknownSkills.isEmpty()) {
                double reqRatio = requiredSkills.isEmpty() ? 1.0 : (double) matchedRequired.size() / requiredSkills.size();
                double prefRatio = preferredSkills.isEmpty() ? 1.0 : (double) matchedPreferred.size() / preferredSkills.size();
                skillsScore = (int) Math.round(40 * (reqRatio * 0.70 + prefRatio * 0.30));
            } else {
                skillsScore = (int) Math.round(40 * ((double) matchedRequired.size() / unknownSkills.size()));
            }
        }

        SkillMatchResult skillMatch = SkillMatchResult.builder()
                .score(skillsScore)
                .matchedRequiredSkills(matchedRequired)
                .missingRequiredSkills(missingRequired)
                .matchedPreferredSkills(matchedPreferred)
                .missingPreferredSkills(missingPreferred)
                .confidence(unknownSkills.isEmpty() ? "HIGH" : "MEDIUM")
                .build();

        // 4. Experience Match
        int requiredYears = extractRequiredYears(jd);
        double detectedYears = calculateDetectedYears(resume);

        int expScore = 25;
        String expRequiredStr = requiredYears == -1 ? "UNKNOWN" : requiredYears + "+";
        String expExplanation;
        String expConfidence;

        if (requiredYears == -1) {
            expExplanation = "No experience requirements detected in the job description.";
            expConfidence = "LOW";
        } else {
            expConfidence = "HIGH";
            if (detectedYears >= requiredYears) {
                expExplanation = String.format("Meets or exceeds the required %d years of experience (Detected: %.1f years).", requiredYears, detectedYears);
            } else {
                expExplanation = String.format("Required %d years of experience, but only %.1f years were detected.", requiredYears, detectedYears);
                expScore = (int) Math.round(25 * (detectedYears / requiredYears));
            }
        }

        ExperienceMatchResult experienceMatch = ExperienceMatchResult.builder()
                .score(expScore)
                .requiredYears(expRequiredStr)
                .detectedYears(detectedYears)
                .explanation(expExplanation)
                .confidence(expConfidence)
                .build();

        // 5. Education Match
        int requiredEduRanking = extractEducationRanking(jd);
        int detectedEduRanking = getResumeEducationRanking(resume);

        int eduScore = 10;
        String requiredDegreeStr = mapRankingToDegreeName(requiredEduRanking);
        String detectedDegreeStr = mapRankingToDegreeName(detectedEduRanking);
        String eduExplanation;
        String eduConfidence;

        if (requiredEduRanking == 0) {
            eduExplanation = "No specific degree requirements detected in the job description.";
            eduConfidence = "LOW";
        } else {
            eduConfidence = "HIGH";
            if (detectedEduRanking >= requiredEduRanking) {
                eduExplanation = String.format("Education matches or exceeds the required level (Required: %s, Detected: %s).", requiredDegreeStr, detectedDegreeStr);
            } else {
                eduExplanation = String.format("Required degree: %s, but only %s was detected.", requiredDegreeStr, detectedDegreeStr);
                eduScore = 5;
            }
        }

        EducationMatchResult educationMatch = EducationMatchResult.builder()
                .score(eduScore)
                .requiredDegree(requiredDegreeStr)
                .detectedDegree(detectedDegreeStr)
                .explanation(eduExplanation)
                .confidence(eduConfidence)
                .build();

        // 6. Keyword Coverage (Max 15)
        List<String> jdKeywords = extractJdKeywords(jd);
        int matchedKeywordsCount = 0;
        if (!jdKeywords.isEmpty()) {
            String rawTextLower = resume.getRawText() != null ? resume.getRawText().toLowerCase() : "";
            for (String keyword : jdKeywords) {
                if (rawTextLower.contains(keyword)) {
                    matchedKeywordsCount++;
                }
            }
        }
        int keywordScore = jdKeywords.isEmpty() ? 15 : (int) Math.round(15 * ((double) matchedKeywordsCount / jdKeywords.size()));

        // 7. Projects Relevance (Max 10)
        int projectScore = 10;
        List<String> allJdSkills = new ArrayList<>();
        allJdSkills.addAll(requiredSkills);
        allJdSkills.addAll(preferredSkills);
        allJdSkills.addAll(unknownSkills);

        if (!allJdSkills.isEmpty() && resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            int projectSkillMatches = 0;
            Set<String> projectSkills = new HashSet<>();
            for (ProjectEntry proj : resume.getProjects()) {
                String projText = ((proj.getProjectName() != null ? proj.getProjectName() : "") + " " +
                        (proj.getDescription() != null ? proj.getDescription() : "")).toLowerCase();
                for (String skill : allJdSkills) {
                    if (projText.contains(skill.toLowerCase())) {
                        projectSkills.add(skill);
                    }
                    if (proj.getTechnologies() != null) {
                        for (String tech : proj.getTechnologies()) {
                            if (tech.equalsIgnoreCase(skill)) {
                                projectSkills.add(skill);
                            }
                        }
                    }
                }
            }
            projectScore = (int) Math.round(10 * ((double) projectSkills.size() / allJdSkills.size()));
        } else if (!allJdSkills.isEmpty()) {
            projectScore = 0; // projects present but none match or no projects listed
        }

        // 8. Overall Score and Recommendations
        int overallScore = skillsScore + expScore + eduScore + keywordScore + projectScore;
        if (overallScore > 100) overallScore = 100;

        List<Recommendation> recommendations = new ArrayList<>();
        if (!missingRequired.isEmpty()) {
            recommendations.add(Recommendation.builder()
                    .priority("HIGH")
                    .category("SKILLS")
                    .message(String.format("Add %s experience if you possess it, as it is required for this role.", String.join(", ", missingRequired)))
                    .build());
        }
        if (!missingPreferred.isEmpty()) {
            recommendations.add(Recommendation.builder()
                    .priority("MEDIUM")
                    .category("SKILLS")
                    .message(String.format("Consider learning or highlighting preferred skills: %s.", String.join(", ", missingPreferred)))
                    .build());
        }
        if (requiredYears != -1 && detectedYears < requiredYears) {
            recommendations.add(Recommendation.builder()
                    .priority("HIGH")
                    .category("EXPERIENCE")
                    .message(String.format("Demonstrate measurable outcomes from your work to support your %.1f years of experience.", detectedYears))
                    .build());
        }
        if (requiredEduRanking > detectedEduRanking) {
            recommendations.add(Recommendation.builder()
                    .priority("MEDIUM")
                    .category("EDUCATION")
                    .message("Clearly state your degrees, major, and certifications in the education section.")
                    .build());
        }
        if (keywordScore < 10) {
            recommendations.add(Recommendation.builder()
                    .priority("LOW")
                    .category("KEYWORDS")
                    .message("Use matching terminology from the job description when describing your previous roles.")
                    .build());
        }

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("skills", skillsScore);
        breakdown.put("experience", expScore);
        breakdown.put("education", eduScore);
        breakdown.put("keywords", keywordScore);
        breakdown.put("projects", projectScore);

        return AtsAnalysisResult.builder()
                .overallScore(overallScore)
                .breakdown(breakdown)
                .skillMatch(skillMatch)
                .experienceMatch(experienceMatch)
                .educationMatch(educationMatch)
                .keywordCoverage(keywordScore)
                .projectRelevance(projectScore)
                .recommendations(recommendations)
                .build();
    }

    private void parseJdSkills(String jd, List<String> required, List<String> preferred, List<String> unknown) {
        String[] lines = jd.split("\\r?\\n");
        String currentSection = "UNKNOWN";

        List<String> sortedDict = new ArrayList<>(SKILL_DICTIONARY);
        sortedDict.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (String line : lines) {
            String lower = line.toLowerCase();
            if (lower.contains("required") || lower.contains("requirements") || lower.contains("must have") || lower.contains("minimum qualification")) {
                currentSection = "REQUIRED";
            } else if (lower.contains("preferred") || lower.contains("nice to have") || lower.contains("plus") || lower.contains("desired")) {
                currentSection = "PREFERRED";
            }

            String tempLine = line;
            for (String dictSkill : sortedDict) {
                if (containsSkill(tempLine, dictSkill)) {
                    if (currentSection.equals("REQUIRED")) {
                        required.add(dictSkill);
                    } else if (currentSection.equals("PREFERRED")) {
                        preferred.add(dictSkill);
                    } else {
                        unknown.add(dictSkill);
                    }
                    tempLine = tempLine.replaceAll("(?i)\\b" + Pattern.quote(dictSkill) + "\\b", "___");
                }
            }
        }
    }

    private List<String> normalizeSkills(List<String> rawSkills) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String skill : rawSkills) {
            String lower = skill.toLowerCase().trim();
            normalized.add(SKILL_NORMALIZATION_MAP.getOrDefault(lower, skill));
        }
        return new ArrayList<>(normalized);
    }

    public static boolean containsSkill(String text, String skillName) {
        if (text == null || skillName == null) return false;
        String patternStr;
        if (skillName.equalsIgnoreCase("C")) {
            patternStr = "(?i)(?:^|\\s|\\p{Punct})C(?![+#])(?:$|\\s|\\p{Punct})";
        } else if (skillName.equalsIgnoreCase("C++")) {
            patternStr = "(?i)(?:^|\\s|\\p{Punct})C\\+\\+(?:$|\\s|\\p{Punct})";
        } else if (skillName.equalsIgnoreCase("C#")) {
            patternStr = "(?i)(?:^|\\s|\\p{Punct})C#(?:$|\\s|\\p{Punct})";
        } else if (skillName.equalsIgnoreCase("Go")) {
            patternStr = "(?i)(?:^|\\s|\\p{Punct})Go(?:$|\\s|\\p{Punct})";
        } else {
            patternStr = "\\b" + Pattern.quote(skillName) + "\\b";
        }
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }

    private int extractRequiredYears(String jd) {
        Pattern pattern = Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(jd);
        int maxYears = -1;
        while (matcher.find()) {
            try {
                int yrs = Integer.parseInt(matcher.group(1));
                if (yrs > maxYears && yrs < 30) {
                    maxYears = yrs;
                }
            } catch (NumberFormatException ignored) {}
        }
        return maxYears;
    }

    private double calculateDetectedYears(ParsedResume resume) {
        if (resume.getExperience() == null || resume.getExperience().isEmpty()) {
            // Check rawText fallback
            if (resume.getRawText() != null) {
                Pattern pattern = Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)\\s+experience\\b", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(resume.getRawText());
                if (matcher.find()) {
                    try {
                        return Double.parseDouble(matcher.group(1));
                    } catch (NumberFormatException ignored) {}
                }
            }
            return 0.0;
        }

        double totalYears = 0.0;
        for (ExperienceEntry entry : resume.getExperience()) {
            int start = extractYear(entry.getStartDate());
            int end = entry.isCurrent() ? LocalDate.now().getYear() : extractYear(entry.getEndDate());
            if (start > 0 && end >= start) {
                totalYears += (end - start);
            }
        }
        return totalYears > 0 ? totalYears : 1.0;
    }

    private int extractYear(String dateStr) {
        if (dateStr == null) return -1;
        Pattern pattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
        Matcher matcher = pattern.matcher(dateStr);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private int extractEducationRanking(String jd) {
        String lower = jd.toLowerCase();
        if (lower.contains("ph.d") || lower.contains("phd") || lower.contains("doctorate")) return 3;
        if (lower.contains("master") || lower.contains("ms") || lower.contains("m.tech") || lower.contains("mba")) return 2;
        if (lower.contains("bachelor") || lower.contains("bs") || lower.contains("b.tech") || lower.contains("b.e") || lower.contains("degree")) return 1;
        return 0;
    }

    private int getResumeEducationRanking(ParsedResume resume) {
        if (resume.getEducation() == null || resume.getEducation().isEmpty()) return 0;
        int maxRank = 1; // default to Bachelor's if they list education
        for (EducationEntry entry : resume.getEducation()) {
            String degree = entry.getDegree() != null ? entry.getDegree().toLowerCase() : "";
            if (degree.contains("ph.d") || degree.contains("phd") || degree.contains("doctorate")) return 3;
            if (degree.contains("master") || degree.contains("ms") || degree.contains("m.tech") || degree.contains("mba")) {
                maxRank = Math.max(maxRank, 2);
            }
        }
        return maxRank;
    }

    private String mapRankingToDegreeName(int rank) {
        switch (rank) {
            case 3: return "PhD";
            case 2: return "Master's";
            case 1: return "Bachelor's";
            default: return "UNKNOWN";
        }
    }

    private List<String> extractJdKeywords(String jd) {
        String cleaned = jd.replaceAll("[^a-zA-Z0-9\\s]", " ").toLowerCase();
        String[] words = cleaned.split("\\s+");
        Map<String, Integer> counts = new HashMap<>();

        for (String w : words) {
            if (w.length() >= 3 && !STOP_WORDS.contains(w)) {
                counts.put(w, counts.getOrDefault(w, 0) + 1);
            }
        }

        return counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(20)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
