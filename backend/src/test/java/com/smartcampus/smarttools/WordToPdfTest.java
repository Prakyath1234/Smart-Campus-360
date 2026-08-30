package com.smartcampus.smarttools;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.exception.DocumentConversionException;
import com.smartcampus.smarttools.service.WordToPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class WordToPdfTest {

    @Autowired
    private WordToPdfService wordToPdfService;

    private byte[] createMockDocx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("[Content_Types].xml");
            zos.putNextEntry(entry);
            zos.write("<Types></Types>".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    void testRealWordToPdfConversionSuccess() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            // Title
            XWPFParagraph title = doc.createParagraph();
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Smart Campus 360 - Word to PDF Conversion Verification");
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            // Heading
            XWPFParagraph heading = doc.createParagraph();
            XWPFRun headingRun = heading.createRun();
            headingRun.setText("1. Verification Details");
            headingRun.setBold(true);
            headingRun.setFontSize(14);

            // Paragraph
            XWPFParagraph p1 = doc.createParagraph();
            XWPFRun r1 = p1.createRun();
            r1.setText("This document is programmatically created to verify the end-to-end integration of LibreOffice with the SmartTools utility platform.");

            // Bullet list
            XWPFParagraph b1 = doc.createParagraph();
            b1.setNumID(BigInteger.valueOf(1));
            XWPFRun br1 = b1.createRun();
            br1.setText("Bullet Point 1: Validating docx zip structure.");
            
            XWPFParagraph b2 = doc.createParagraph();
            b2.setNumID(BigInteger.valueOf(1));
            XWPFRun br2 = b2.createRun();
            br2.setText("Bullet Point 2: Validating ProcessBuilder invocation.");

            // Table
            XWPFTable table = doc.createTable(2, 2);
            table.getRow(0).getCell(0).setText("Feature");
            table.getRow(0).getCell(1).setText("Status");
            table.getRow(1).getCell(0).setText("LibreOffice Headless");
            table.getRow(1).getCell(1).setText("Verified");

            // Page break
            XWPFParagraph pPageBreak = doc.createParagraph();
            pPageBreak.createRun().addBreak(BreakType.PAGE);

            // Second Page Content
            XWPFParagraph p2 = doc.createParagraph();
            p2.createRun().setText("This is page 2 of the verification document.");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.write(baos);

            byte[] docxBytes = baos.toByteArray();
            MockMultipartFile file = new MockMultipartFile("file", "verify.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes);

            // Run conversion
            byte[] pdfBytes = wordToPdfService.convertWordToPdf(file);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);

            // Validate using PDFBox
            try (PDDocument pdfDoc = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
                assertEquals(2, pdfDoc.getNumberOfPages());
            }
        }
    }

    @Test
    void testWordValidationUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "Dummy content".getBytes());
        assertThrows(BadRequestException.class, () -> wordToPdfService.convertWordToPdf(file));
    }

    @Test
    void testWordValidationEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> wordToPdfService.convertWordToPdf(file));
    }

    @Test
    void testWordValidationCorruptedDocx() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Fake zip content".getBytes());
        assertThrows(BadRequestException.class, () -> wordToPdfService.convertWordToPdf(file));
    }
}
