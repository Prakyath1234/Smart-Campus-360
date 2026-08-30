package com.smartcampus.smarttools.service;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.exception.DocumentProcessingException;
import com.smartcampus.smarttools.model.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private final FileValidationService fileValidationService;

    // Categorized Skill Dictionary
    private static final Map<String, List<String>> SKILL_DICTIONARY = new LinkedHashMap<>();
    static {
        SKILL_DICTIONARY.put("Programming Languages", Arrays.asList("Java", "Python", "C++", "C", "Go", "Kotlin", "SQL", "JavaScript", "TypeScript"));
        SKILL_DICTIONARY.put("Frameworks", Arrays.asList("Spring Boot", "Spring", "React", "Angular", "Node.js", "Express", "Hibernate"));
        SKILL_DICTIONARY.put("Databases", Arrays.asList("MySQL", "PostgreSQL", "MongoDB", "Oracle", "Redis"));
        SKILL_DICTIONARY.put("Cloud", Arrays.asList("AWS", "Azure", "GCP"));
        SKILL_DICTIONARY.put("Tools", Arrays.asList("Git", "GitHub", "Docker", "Kubernetes", "Maven", "Gradle", "Jenkins"));
    }

    public ResumeParserService(FileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }

    public ParsedResume parseResume(MultipartFile file, boolean includeRawText) {
        fileValidationService.validateFile(file);

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BadRequestException("Invalid filename.");
        }
        String lowerName = originalName.toLowerCase();
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".docx")) {
            throw new BadRequestException("Only PDF and DOCX resume documents are supported.");
        }

        String rawText = extractRawText(file, lowerName);
        if (rawText == null || rawText.trim().isEmpty()) {
            throw new BadRequestException("No readable text found in the resume.");
        }

        return parseResumeFromText(rawText, includeRawText);
    }

    private String extractRawText(MultipartFile file, String originalName) {
        try {
            if (originalName.endsWith(".pdf")) {
                try (InputStream is = file.getInputStream();
                     PDDocument doc = org.apache.pdfbox.Loader.loadPDF(is.readAllBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(doc);
                }
            } else {
                try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                    StringBuilder sb = new StringBuilder();
                    for (XWPFParagraph p : doc.getParagraphs()) {
                        sb.append(p.getText()).append("\n");
                    }
                    return sb.toString();
                }
            }
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to read text from resume file: " + e.getMessage(), e);
        }
    }

    private ParsedResume parseResumeFromText(String rawText, boolean includeRawText) {
        // Scanned Document check
        if (rawText.trim().length() < 25) {
            throw new BadRequestException("This PDF appears to contain scanned/image-only pages. OCR is required to extract editable text.");
        }

        // Section Detection Map
        Map<String, List<String>> sections = segmentSections(rawText);

        // Profile & Contacts
        ContactInformation contact = parseContactInformation(rawText);
        String fullName = parseFullName(rawText);
        String summary = parseSummary(sections);

        ResumeProfile profile = ResumeProfile.builder()
                .fullName(fullName)
                .summary(summary)
                .contact(contact)
                .build();

        // Skills Matcher
        Map<String, List<String>> skills = extractSkills(rawText);

        // Education Matcher
        List<EducationEntry> education = extractEducation(sections.get("EDUCATION"));

        // Experience Matcher
        List<ExperienceEntry> experience = extractExperience(sections.get("EXPERIENCE"));

        // Project Matcher
        List<ProjectEntry> projects = extractProjects(sections.get("PROJECTS"));

        // Certifications Matcher
        List<CertificationEntry> certifications = extractCertifications(sections.get("CERTIFICATIONS"));

        // Achievements Matcher
        List<String> achievements = extractAchievements(sections.get("ACHIEVEMENTS"));

        // Confidence Logic
        Map<String, String> confidence = new HashMap<>();
        confidence.put("profile", profile.getFullName() != null && contact.getEmail() != null ? "HIGH" : "MEDIUM");
        confidence.put("skills", !skills.isEmpty() ? "HIGH" : "LOW");
        confidence.put("education", !education.isEmpty() ? "HIGH" : "LOW");
        confidence.put("experience", !experience.isEmpty() ? "HIGH" : "LOW");

        return ParsedResume.builder()
                .profile(profile)
                .skills(skills)
                .education(education)
                .experience(experience)
                .projects(projects)
                .certifications(certifications)
                .achievements(achievements)
                .confidence(confidence)
                .rawText(includeRawText ? rawText : null)
                .build();
    }

    private Map<String, List<String>> segmentSections(String rawText) {
        Map<String, List<String>> sections = new HashMap<>();
        String[] lines = rawText.split("\\r?\\n");
        String currentSection = "HEADER";

        sections.put("HEADER", new ArrayList<>());
        sections.put("SUMMARY", new ArrayList<>());
        sections.put("EDUCATION", new ArrayList<>());
        sections.put("EXPERIENCE", new ArrayList<>());
        sections.put("SKILLS", new ArrayList<>());
        sections.put("PROJECTS", new ArrayList<>());
        sections.put("CERTIFICATIONS", new ArrayList<>());
        sections.put("ACHIEVEMENTS", new ArrayList<>());

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String sectionHeader = detectSectionHeader(trimmed);
            if (sectionHeader != null) {
                currentSection = sectionHeader;
            } else {
                sections.get(currentSection).add(trimmed);
            }
        }
        return sections;
    }

    private String detectSectionHeader(String line) {
        String clean = line.replaceAll("[^a-zA-Z\\s]", "").toUpperCase().trim();
        if (clean.equals("SUMMARY") || clean.equals("OBJECTIVE") || clean.equals("PROFILE") || clean.equals("ABOUT ME")) {
            return "SUMMARY";
        }
        if (clean.equals("EDUCATION") || clean.equals("ACADEMIC BACKGROUND") || clean.equals("ACADEMICS")) {
            return "EDUCATION";
        }
        if (clean.equals("EXPERIENCE") || clean.equals("WORK EXPERIENCE") || clean.equals("EMPLOYMENT") || clean.equals("WORK HISTORY")) {
            return "EXPERIENCE";
        }
        if (clean.equals("SKILLS") || clean.equals("TECHNICAL SKILLS") || clean.equals("SKILLS  TECHNOLOGIES")) {
            return "SKILLS";
        }
        if (clean.equals("PROJECTS") || clean.equals("ACADEMIC PROJECTS")) {
            return "PROJECTS";
        }
        if (clean.equals("CERTIFICATIONS") || clean.equals("CERTIFICATES")) {
            return "CERTIFICATIONS";
        }
        if (clean.equals("ACHIEVEMENTS") || clean.equals("AWARDS") || clean.equals("HONORS")) {
            return "ACHIEVEMENTS";
        }
        return null;
    }

    private ContactInformation parseContactInformation(String text) {
        String email = null;
        String phone = null;
        String linkedin = null;
        String github = null;
        String portfolio = null;

        // Email regex
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}");
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            email = emailMatcher.group().trim();
        }

        // Phone regex
        Pattern phonePattern = Pattern.compile("(?:\\+?\\d{1,3}[-\\s]?)?\\(?\\d{3,5}\\)?[-\\s]?\\d{3,4}[-\\s]?\\d{3,4}");
        Matcher phoneMatcher = phonePattern.matcher(text);
        if (phoneMatcher.find()) {
            phone = phoneMatcher.group().trim();
        }

        // LinkedIn regex
        Pattern linkedinPattern = Pattern.compile("linkedin\\.com/in/[a-zA-Z0-9_-]+");
        Matcher linkedinMatcher = linkedinPattern.matcher(text);
        if (linkedinMatcher.find()) {
            linkedin = "https://" + linkedinMatcher.group().trim();
        }

        // GitHub regex
        Pattern githubPattern = Pattern.compile("github\\.com/[a-zA-Z0-9_-]+");
        Matcher githubMatcher = githubPattern.matcher(text);
        if (githubMatcher.find()) {
            github = "https://" + githubMatcher.group().trim();
        }

        // Portfolio regex
        Pattern portfolioPattern = Pattern.compile("(?:https?://)?(?:www\\.)?([a-zA-Z0-9-]+(?!github|linkedin)\\.[a-zA-Z]{2,6}/?[a-zA-Z0-9_-]*)");
        Matcher portfolioMatcher = portfolioPattern.matcher(text);
        if (portfolioMatcher.find()) {
            portfolio = portfolioMatcher.group().trim();
            if (!portfolio.startsWith("http")) {
                portfolio = "https://" + portfolio;
            }
        }

        return ContactInformation.builder()
                .email(email)
                .phone(phone)
                .linkedin(linkedin)
                .github(github)
                .portfolio(portfolio)
                .location("Not Found")
                .build();
    }

    private String parseFullName(String text) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.contains("@") || trimmed.contains("Resume") || trimmed.contains("Page")) {
                continue;
            }
            // Check if it looks like a name (e.g. 2-3 capitalized words)
            if (trimmed.matches("^[A-Z][a-z]+(\\s[A-Z][a-z]+){1,2}$")) {
                return trimmed;
            }
        }
        return "Unknown Name";
    }

    private String parseSummary(Map<String, List<String>> sections) {
        List<String> summaryLines = sections.get("SUMMARY");
        if (summaryLines == null || summaryLines.isEmpty()) {
            return "No summary provided.";
        }
        return String.join(" ", summaryLines);
    }

    private Map<String, List<String>> extractSkills(String text) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : SKILL_DICTIONARY.entrySet()) {
            List<String> matched = new ArrayList<>();
            for (String skill : entry.getValue()) {
                // Token boundaries matcher. Treat C++ and Spring Boot correctly.
                String escaped = Pattern.quote(skill);
                Pattern pattern = Pattern.compile("\\b" + escaped + "\\b", Pattern.CASE_INSENSITIVE);
                if (skill.equals("C++") || skill.equals("C#")) {
                    pattern = Pattern.compile(escaped, Pattern.CASE_INSENSITIVE);
                } else if (skill.equals("C")) {
                    pattern = Pattern.compile("\\bC\\b"); // Case sensitive C check
                }
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    matched.add(skill);
                }
            }
            if (!matched.isEmpty()) {
                result.put(entry.getKey(), matched);
            }
        }
        return result;
    }

    private List<EducationEntry> extractEducation(List<String> lines) {
        List<EducationEntry> result = new ArrayList<>();
        if (lines == null) return result;

        EducationEntry current = null;
        for (String line : lines) {
            String clean = line.toUpperCase();
            String degree = null;
            if (clean.contains("B.TECH") || clean.contains("BACHELOR OF TECHNOLOGY")) degree = "B.Tech";
            else if (clean.contains("B.E.") || clean.contains("BACHELOR OF ENGINEERING")) degree = "B.E.";
            else if (clean.contains("MCA") || clean.contains("MASTER OF COMPUTER APPLICATIONS")) degree = "MCA";
            else if (clean.contains("BCA")) degree = "BCA";
            else if (clean.contains("M.TECH")) degree = "M.Tech";

            if (degree != null) {
                if (current != null) {
                    result.add(current);
                }
                current = EducationEntry.builder()
                        .degree(degree)
                        .fieldOfStudy(clean.contains("COMPUTER") ? "Computer Science" : "Engineering")
                        .institution("University / Institution")
                        .build();
            }

            if (current != null) {
                // Extract Institution name
                if (line.contains("University") || line.contains("College") || line.contains("Institute") || line.contains("School")) {
                    current.setInstitution(line.trim());
                }
                // Extract Year
                Pattern yearPattern = Pattern.compile("\\b(20\\d{2})\\b");
                Matcher m = yearPattern.matcher(line);
                if (m.find()) {
                    if (current.getEndYear() == null) {
                        current.setEndYear(m.group());
                    } else if (current.getStartYear() == null) {
                        current.setStartYear(m.group());
                    }
                }
                // CGPA
                if (clean.contains("CGPA") || clean.contains("GPA")) {
                    Pattern cgpaPattern = Pattern.compile("\\b\\d\\.\\d{1,2}\\b");
                    Matcher cm = cgpaPattern.matcher(line);
                    if (cm.find()) {
                        current.setCgpa(cm.group());
                    }
                }
            }
        }
        if (current != null) {
            result.add(current);
        }
        return result;
    }

    private List<ExperienceEntry> extractExperience(List<String> lines) {
        List<ExperienceEntry> result = new ArrayList<>();
        if (lines == null) return result;

        ExperienceEntry current = null;
        for (String line : lines) {
            // Check if this line looks like a job title / company layout
            if (line.contains("Developer") || line.contains("Engineer") || line.contains("Architect") || line.contains("Intern")) {
                if (current != null) {
                    result.add(current);
                }
                current = ExperienceEntry.builder()
                        .title(line.trim())
                        .company("Company Name")
                        .description(new ArrayList<>())
                        .build();
            }

            if (current != null) {
                if (line.contains("Present") || line.contains("Current") || line.contains("Till Date")) {
                    current.setCurrent(true);
                }
                // Simple description collector
                if (line.trim().startsWith("-") || line.trim().startsWith("*") || line.length() > 40) {
                    current.getDescription().add(line.trim());
                }
            }
        }
        if (current != null) {
            result.add(current);
        }
        return result;
    }

    private List<ProjectEntry> extractProjects(List<String> lines) {
        List<ProjectEntry> result = new ArrayList<>();
        if (lines == null) return result;

        ProjectEntry current = null;
        for (String line : lines) {
            if (line.contains("Project") || line.contains("Smart") || line.length() < 30 && line.matches("^[A-Z][a-zA-Z\\s]+$")) {
                if (current != null) {
                    result.add(current);
                }
                current = ProjectEntry.builder()
                        .projectName(line.trim())
                        .technologies(new ArrayList<>())
                        .build();
            }

            if (current != null) {
                if (line.contains("Technologies") || line.contains("Tech:")) {
                    current.getTechnologies().add(line.trim());
                } else if (line.length() > 20) {
                    current.setDescription(line.trim());
                }
            }
        }
        if (current != null) {
            result.add(current);
        }
        return result;
    }

    private List<CertificationEntry> extractCertifications(List<String> lines) {
        List<CertificationEntry> result = new ArrayList<>();
        if (lines == null) return result;

        for (String line : lines) {
            if (line.contains("Certified") || line.contains("Certificate") || line.contains("AWS") || line.contains("Java")) {
                result.add(CertificationEntry.builder()
                        .name(line.trim())
                        .issuer("Issuer")
                        .build());
            }
        }
        return result;
    }

    private List<String> extractAchievements(List<String> lines) {
        List<String> result = new ArrayList<>();
        if (lines == null) return result;
        for (String line : lines) {
            if (line.length() > 10) {
                result.add(line.trim());
            }
        }
        return result;
    }
}
