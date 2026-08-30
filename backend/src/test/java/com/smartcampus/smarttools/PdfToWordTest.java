package com.smartcampus.smarttools;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.service.PdfToWordService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class PdfToWordTest {

    @Autowired
    private PdfToWordService pdfToWordService;

    private byte[] createTextPdf(String title, String heading, String body) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(title);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText(heading);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 600);
                contentStream.showText(body);
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
    void testRealPdfToWordConversionSuccess() throws Exception {
        String title = "Smart Campus Title Document";
        String heading = "Heading 1: Verification Info";
        String body = "This is paragraph text that verifies character retention.";

        byte[] pdfBytes = createTextPdf(title, heading, body);
        MockMultipartFile file = new MockMultipartFile("file", "verify.pdf", "application/pdf", pdfBytes);

        // Convert
        byte[] docxBytes = pdfToWordService.convertPdfToWord(file);
        assertNotNull(docxBytes);
        assertTrue(docxBytes.length > 0);

        // Open docx and verify text
        try (XWPFDocument checkDoc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : checkDoc.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            String extractedText = sb.toString();

            assertTrue(extractedText.contains(title));
            assertTrue(extractedText.contains(heading));
            assertTrue(extractedText.contains(body));

            // Basic character retention calculation
            int sourceCharacters = title.length() + heading.length() + body.length();
            int retainedCharacters = 0;
            if (extractedText.contains(title)) retainedCharacters += title.length();
            if (extractedText.contains(heading)) retainedCharacters += heading.length();
            if (extractedText.contains(body)) retainedCharacters += body.length();

            double retentionPercentage = ((double) retainedCharacters / sourceCharacters) * 100;
            System.out.println("Character Retention Percentage: " + retentionPercentage + "%");
            assertEquals(100.0, retentionPercentage);
        }
    }

    @Test
    void testScannedPdfDetection() throws Exception {
        byte[] pdfBytes = createScannedPdf();
        MockMultipartFile file = new MockMultipartFile("file", "scanned.pdf", "application/pdf", pdfBytes);

        assertThrows(BadRequestException.class, () -> pdfToWordService.convertPdfToWord(file));
    }

    @Test
    void testPdfValidationUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "document.doc", "application/msword", "Dummy content".getBytes());
        assertThrows(BadRequestException.class, () -> pdfToWordService.convertPdfToWord(file));
    }

    @Test
    void testPdfValidationEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> pdfToWordService.convertPdfToWord(file));
    }

    @Test
    void testPdfValidationCorruptedFile() {
        MockMultipartFile file = new MockMultipartFile("file", "corrupt.pdf", "application/pdf", "Fake pdf contents".getBytes());
        assertThrows(RuntimeException.class, () -> pdfToWordService.convertPdfToWord(file));
    }
}
