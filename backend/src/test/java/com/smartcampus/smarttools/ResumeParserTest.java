package com.smartcampus.smarttools;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.model.ParsedResume;
import com.smartcampus.smarttools.service.ResumeParserService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class ResumeParserTest {

    @Autowired
    private ResumeParserService resumeParserService;

    private byte[] createResumePdf(String name, String email, String phone, String skillsSection, String eduSection, String expSection) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(name);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Email: " + email + " | Phone: " + phone);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("LinkedIn: linkedin.com/in/johndoe | GitHub: github.com/johndoe");
                contentStream.endText();

                // Skills Heading
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 660);
                contentStream.showText("SKILLS");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 640);
                contentStream.showText(skillsSection);
                contentStream.endText();

                // Education Heading
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 600);
                contentStream.showText("EDUCATION");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 580);
                contentStream.showText(eduSection);
                contentStream.endText();

                // Experience Heading
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 540);
                contentStream.showText("EXPERIENCE");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 520);
                contentStream.showText(expSection);
                contentStream.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private byte[] createScannedPdf() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.drawImage(pdImage, 50, 500, 100, 100);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void testPdfResumeParsingSuccess() throws Exception {
        String name = "John Doe";
        String email = "john@sode-edu.in";
        String phone = "+91 9876543210";
        String skills = "Java, Spring Boot, React, MySQL, AWS, Git";
        String education = "B.Tech in Computer Science from VTU University 2024 CGPA 9.2";
        String experience = "Java Developer at SODE Tech, 2023 to Present";

        byte[] pdfBytes = createResumePdf(name, email, phone, skills, education, experience);
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        ParsedResume parsed = resumeParserService.parseResume(file, false);
        assertNotNull(parsed);

        assertEquals(name, parsed.getProfile().getFullName());
        assertEquals(email, parsed.getProfile().getContact().getEmail());
        assertEquals(phone, parsed.getProfile().getContact().getPhone());

        // Skills Check
        assertTrue(parsed.getSkills().get("Programming Languages").contains("Java"));
        assertTrue(parsed.getSkills().get("Frameworks").contains("React"));

        // Education Check
        assertFalse(parsed.getEducation().isEmpty());
        assertEquals("B.Tech", parsed.getEducation().get(0).getDegree());

        // Experience Check
        assertFalse(parsed.getExperience().isEmpty());
        assertTrue(parsed.getExperience().get(0).isCurrent());
    }

    @Test
    void testScannedPdfResumeRejection() throws Exception {
        byte[] pdfBytes = createScannedPdf();
        MockMultipartFile file = new MockMultipartFile("file", "scanned.pdf", "application/pdf", pdfBytes);

        assertThrows(BadRequestException.class, () -> resumeParserService.parseResume(file, false));
    }

    @Test
    void testInvalidFileFormat() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "Dummy contents".getBytes());
        assertThrows(BadRequestException.class, () -> resumeParserService.parseResume(file, false));
    }

    @Test
    void testEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> resumeParserService.parseResume(file, false));
    }
}
