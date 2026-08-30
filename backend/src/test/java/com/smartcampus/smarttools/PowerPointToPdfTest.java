package com.smartcampus.smarttools;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.service.PowerPointToPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xslf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class PowerPointToPdfTest {

    @Autowired
    private PowerPointToPdfService powerPointToPdfService;

    private byte[] createMockPptx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("ppt/presentation.xml");
            zos.putNextEntry(entry);
            zos.write("<presentation></presentation>".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    void testRealPowerPointToPdfConversionSuccess() throws Exception {
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            // Slide 1: Title
            XSLFSlide slide1 = ppt.createSlide();
            XSLFTextBox titleBox = slide1.createTextBox();
            titleBox.setAnchor(new Rectangle(50, 50, 500, 100));
            XSLFTextParagraph titlePara = titleBox.addNewTextParagraph();
            XSLFTextRun titleRun = titlePara.addNewTextRun();
            titleRun.setText("Smart Campus 360 Verification Presentation");
            titleRun.setBold(true);
            titleRun.setFontSize(24.0);

            // Slide 2: Heading, Paragraph, Bullet list
            XSLFSlide slide2 = ppt.createSlide();
            XSLFTextBox box2 = slide2.createTextBox();
            box2.setAnchor(new Rectangle(50, 50, 500, 300));
            
            XSLFTextParagraph headPara = box2.addNewTextParagraph();
            XSLFTextRun headRun = headPara.addNewTextRun();
            headRun.setText("1. PPTX Integration Verification");
            headRun.setBold(true);
            headRun.setFontSize(18.0);

            XSLFTextParagraph bodyPara = box2.addNewTextParagraph();
            XSLFTextRun bodyRun = bodyPara.addNewTextRun();
            bodyRun.setText("This presentation is programmatically generated to execute and verify the PowerPoint to PDF converter service.");
            bodyRun.setFontSize(14.0);

            XSLFTextParagraph bullet1 = box2.addNewTextParagraph();
            bullet1.setBullet(true);
            XSLFTextRun br1 = bullet1.addNewTextRun();
            br1.setText("Point A: Verifying OfficeToPdfService shared process engine.");
            
            XSLFTextParagraph bullet2 = box2.addNewTextParagraph();
            bullet2.setBullet(true);
            XSLFTextRun br2 = bullet2.addNewTextRun();
            br2.setText("Point B: Verifying PDFBox slide page validation.");

            // Slide 3: Table
            XSLFSlide slide3 = ppt.createSlide();
            XSLFTable table = slide3.createTable(2, 2);
            table.setAnchor(new Rectangle(50, 50, 400, 100));
            table.getCell(0, 0).setText("Parameter");
            table.getCell(0, 1).setText("Value");
            table.getCell(1, 0).setText("Slides Count");
            table.getCell(1, 1).setText("3");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ppt.write(baos);

            byte[] pptxBytes = baos.toByteArray();
            MockMultipartFile file = new MockMultipartFile("file", "verify.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxBytes);

            // Convert
            byte[] pdfBytes = powerPointToPdfService.convertPowerPointToPdf(file);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);

            // Verify PDFBox
            try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
                assertEquals(3, doc.getNumberOfPages());
            }
        }
    }

    @Test
    void testPowerPointValidationUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "Dummy content".getBytes());
        assertThrows(BadRequestException.class, () -> powerPointToPdfService.convertPowerPointToPdf(file));
    }

    @Test
    void testPowerPointValidationEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> powerPointToPdfService.convertPowerPointToPdf(file));
    }

    @Test
    void testPowerPointValidationCorruptedPptx() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "Fake zip content".getBytes());
        assertThrows(BadRequestException.class, () -> powerPointToPdfService.convertPowerPointToPdf(file));
    }
}
