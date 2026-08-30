package com.smartcampus.smarttools;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.service.ExcelToPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class ExcelToPdfTest {

    @Autowired
    private ExcelToPdfService excelToPdfService;

    private byte[] createMockXlsx() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("xl/workbook.xml");
            zos.putNextEntry(entry);
            zos.write("<workbook></workbook>".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    void testRealExcelToPdfConversionSuccess() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Students
            XSSFSheet sheet1 = workbook.createSheet("Students");
            XSSFRow row0 = sheet1.createRow(0);
            row0.createCell(0).setCellValue("Name");
            row0.createCell(1).setCellValue("Department");
            row0.createCell(2).setCellValue("Semester");
            row0.createCell(3).setCellValue("Marks");

            XSSFRow row1 = sheet1.createRow(1);
            row1.createCell(0).setCellValue("Manoj");
            row1.createCell(1).setCellValue("CSE");
            row1.createCell(2).setCellValue(5);
            row1.createCell(3).setCellValue(85.5);

            // Sheet 2: Attendance
            XSSFSheet sheet2 = workbook.createSheet("Attendance");
            XSSFRow attRow0 = sheet2.createRow(0);
            attRow0.createCell(0).setCellValue("Date");
            attRow0.createCell(1).setCellValue("Status");

            XSSFRow attRow1 = sheet2.createRow(1);
            attRow1.createCell(0).setCellValue(new Date());
            attRow1.createCell(1).setCellValue("Present");

            // Sheet 3: Summary (Formulas)
            XSSFSheet sheet3 = workbook.createSheet("Summary");
            XSSFRow sumRow0 = sheet3.createRow(0);
            sumRow0.createCell(0).setCellValue(10);
            XSSFRow sumRow1 = sheet3.createRow(1);
            sumRow1.createCell(0).setCellValue(20);
            XSSFRow sumRow2 = sheet3.createRow(2);
            // Formula A3 = SUM(A1:A2)
            sumRow2.createCell(0).setCellFormula("SUM(A1:A2)");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            byte[] xlsxBytes = baos.toByteArray();
            MockMultipartFile file = new MockMultipartFile("file", "verify.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);

            // Convert Excel to PDF
            byte[] pdfBytes = excelToPdfService.convertExcelToPdf(file);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 0);

            // Verify with PDFBox
            try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
                assertTrue(doc.getNumberOfPages() > 0);
            }
        }
    }

    @Test
    void testExcelValidationUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "Dummy content".getBytes());
        assertThrows(BadRequestException.class, () -> excelToPdfService.convertExcelToPdf(file));
    }

    @Test
    void testExcelValidationEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> excelToPdfService.convertExcelToPdf(file));
    }

    @Test
    void testExcelValidationCorruptedXlsx() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Fake zip content".getBytes());
        assertThrows(BadRequestException.class, () -> excelToPdfService.convertExcelToPdf(file));
    }
}
