package com.smartcampus.smarttools;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.service.PdfToolService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class PdfToolTest {

    @Autowired
    private PdfToolService pdfToolService;

    private byte[] createMockPdf(int pages) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            for (int i = 0; i < pages; i++) {
                doc.addPage(new PDPage());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    @Test
    void testMergePdfFilesSuccess() throws IOException {
        byte[] pdf1Bytes = createMockPdf(1);
        byte[] pdf2Bytes = createMockPdf(1);

        MockMultipartFile file1 = new MockMultipartFile("files", "doc1.pdf", "application/pdf", pdf1Bytes);
        MockMultipartFile file2 = new MockMultipartFile("files", "doc2.pdf", "application/pdf", pdf2Bytes);

        List<MultipartFile> list = Arrays.asList(file1, file2);
        byte[] mergedResult = pdfToolService.mergePdfFiles(list);

        assertNotNull(mergedResult);
        assertTrue(mergedResult.length > 0);

        try (PDDocument mergedDoc = org.apache.pdfbox.Loader.loadPDF(mergedResult)) {
            assertEquals(2, mergedDoc.getNumberOfPages());
        }
    }

    @Test
    void testSplitPdfEveryPage() throws IOException {
        byte[] pdfBytes = createMockPdf(3);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        byte[] zipBytes = pdfToolService.splitPdf(file, "EVERY_PAGE", null);
        assertNotNull(zipBytes);

        // Verify ZIP contents
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                count++;
                assertTrue(entry.getName().startsWith("page-"));
                assertTrue(entry.getName().endsWith(".pdf"));
            }
        }
        assertEquals(3, count);
    }

    @Test
    void testSplitPdfPageRanges() throws IOException {
        byte[] pdfBytes = createMockPdf(10);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        byte[] zipBytes = pdfToolService.splitPdf(file, "PAGE_RANGES", "1-3,5,8-10");
        assertNotNull(zipBytes);

        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                count++;
                String name = entry.getName();
                assertTrue(name.equals("pages-1-3.pdf") || name.equals("page-5.pdf") || name.equals("pages-8-10.pdf"));
            }
        }
        assertEquals(3, count);
    }

    @Test
    void testSplitPdfInvalidRanges() throws IOException {
        byte[] pdfBytes = createMockPdf(5);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        // Test page zero
        assertThrows(BadRequestException.class, () -> pdfToolService.splitPdf(file, "PAGE_RANGES", "0-3"));
        // Test negative page bounds
        assertThrows(BadRequestException.class, () -> pdfToolService.splitPdf(file, "PAGE_RANGES", "-1-3"));
        // Test page beyond range
        assertThrows(BadRequestException.class, () -> pdfToolService.splitPdf(file, "PAGE_RANGES", "1-6"));
        // Test overlapping ranges
        assertThrows(BadRequestException.class, () -> pdfToolService.splitPdf(file, "PAGE_RANGES", "1-3,3-5"));
    }

    @Test
    void testExtractPagesSuccess() throws IOException {
        byte[] pdfBytes = createMockPdf(5);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        byte[] result = pdfToolService.extractPages(file, Arrays.asList(1, 3, 5));
        assertNotNull(result);

        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(result)) {
            assertEquals(3, doc.getNumberOfPages());
        }
    }

    @Test
    void testExtractPagesInvalidInputs() throws IOException {
        byte[] pdfBytes = createMockPdf(5);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        // Empty list
        assertThrows(BadRequestException.class, () -> pdfToolService.extractPages(file, Collections.emptyList()));
        // Outside range
        assertThrows(BadRequestException.class, () -> pdfToolService.extractPages(file, Arrays.asList(1, 6)));
        // Negative page
        assertThrows(BadRequestException.class, () -> pdfToolService.extractPages(file, Arrays.asList(-1, 3)));
    }

    @Test
    void testRotatePdfSuccess() throws IOException {
        byte[] pdfBytes = createMockPdf(2);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        byte[] result = pdfToolService.rotatePdf(file, 90);
        assertNotNull(result);

        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(result)) {
            assertEquals(90, doc.getPage(0).getRotation());
            assertEquals(90, doc.getPage(1).getRotation());
        }
    }

    @Test
    void testRotatePdfInvalidDegree() throws IOException {
        byte[] pdfBytes = createMockPdf(2);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        assertThrows(BadRequestException.class, () -> pdfToolService.rotatePdf(file, 45));
        assertThrows(BadRequestException.class, () -> pdfToolService.rotatePdf(file, -90));
    }

    @Test
    void testReorderPdfSuccess() throws IOException {
        byte[] pdfBytes = createMockPdf(5);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        byte[] result = pdfToolService.reorderPdf(file, Arrays.asList(5, 1, 3, 2, 4));
        assertNotNull(result);

        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(result)) {
            assertEquals(5, doc.getNumberOfPages());
        }
    }

    @Test
    void testReorderPdfInvalidInputs() throws IOException {
        byte[] pdfBytes = createMockPdf(5);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", pdfBytes);

        // Missing pages
        assertThrows(BadRequestException.class, () -> pdfToolService.reorderPdf(file, Arrays.asList(5, 1, 3)));
        // Duplicate page
        assertThrows(BadRequestException.class, () -> pdfToolService.reorderPdf(file, Arrays.asList(1, 2, 3, 3, 4)));
        // Invalid page beyond bounds
        assertThrows(BadRequestException.class, () -> pdfToolService.reorderPdf(file, Arrays.asList(7, 1, 2, 3, 4)));
    }

    @Test
    void testSecurityMalformedPdfRejected() {
        MockMultipartFile file = new MockMultipartFile("file", "malicious.pdf", "application/pdf", "Not a real PDF contents".getBytes());
        assertThrows(BadRequestException.class, () -> pdfToolService.splitPdf(file, "EVERY_PAGE", null));
    }
}
