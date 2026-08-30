package com.smartcampus.smarttools;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.service.ImageToPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class ImageToPdfTest {

    @Autowired
    private ImageToPdfService imageToPdfService;

    private byte[] generateImageBytes(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    void testSinglePngConversion() throws Exception {
        byte[] bytes = generateImageBytes("png", 100, 200); // Tall portrait
        MockMultipartFile file = new MockMultipartFile("files", "test.png", "image/png", bytes);

        byte[] pdfBytes = imageToPdfService.convertImagesToPdf(
                List.of(file), null, "A4", "AUTO", "FIT", "NONE"
        );

        assertNotNull(pdfBytes);
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
            assertEquals(1, doc.getNumberOfPages());
        }
    }

    @Test
    void testSingleJpgConversion() throws Exception {
        byte[] bytes = generateImageBytes("jpeg", 300, 200); // Wide landscape
        MockMultipartFile file = new MockMultipartFile("files", "test.jpg", "image/jpeg", bytes);

        byte[] pdfBytes = imageToPdfService.convertImagesToPdf(
                List.of(file), null, "LETTER", "LANDSCAPE", "FILL", "MEDIUM"
        );

        assertNotNull(pdfBytes);
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
            assertEquals(1, doc.getNumberOfPages());
        }
    }

    @Test
    void testMultipleImagesAndCustomOrder() throws Exception {
        byte[] pngBytes = generateImageBytes("png", 100, 100);
        byte[] jpgBytes = generateImageBytes("jpeg", 150, 150);

        MockMultipartFile file1 = new MockMultipartFile("files", "one.png", "image/png", pngBytes);
        MockMultipartFile file2 = new MockMultipartFile("files", "two.jpg", "image/jpeg", jpgBytes);

        // Upload order
        byte[] pdfBytesUpload = imageToPdfService.convertImagesToPdf(
                List.of(file1, file2), null, "ORIGINAL", "AUTO", "FIT", "NONE"
        );
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfBytesUpload)) {
            assertEquals(2, doc.getNumberOfPages());
        }

        // Custom order (2nd first, then 1st)
        byte[] pdfBytesCustom = imageToPdfService.convertImagesToPdf(
                List.of(file1, file2), Arrays.asList(1, 0), "A4", "PORTRAIT", "FIT", "LARGE"
        );
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfBytesCustom)) {
            assertEquals(2, doc.getNumberOfPages());
        }
    }

    @Test
    void testInvalidOrderSequences() throws Exception {
        byte[] pngBytes = generateImageBytes("png", 100, 100);
        byte[] jpgBytes = generateImageBytes("jpeg", 150, 150);

        MockMultipartFile file1 = new MockMultipartFile("files", "one.png", "image/png", pngBytes);
        MockMultipartFile file2 = new MockMultipartFile("files", "two.jpg", "image/jpeg", jpgBytes);

        List<MultipartFile> files = List.of(file1, file2);

        // Size mismatch
        assertThrows(BadRequestException.class, () ->
                imageToPdfService.convertImagesToPdf(files, List.of(0), "A4", "AUTO", "FIT", "NONE")
        );

        // Duplicate index
        assertThrows(BadRequestException.class, () ->
                imageToPdfService.convertImagesToPdf(files, List.of(0, 0), "A4", "AUTO", "FIT", "NONE")
        );

        // Out of range index
        assertThrows(BadRequestException.class, () ->
                imageToPdfService.convertImagesToPdf(files, List.of(0, 2), "A4", "AUTO", "FIT", "NONE")
        );
    }

    @Test
    void testInvalidAndUnsupportedFormats() {
        MockMultipartFile badFile = new MockMultipartFile("files", "test.txt", "text/plain", "Not an image".getBytes());
        assertThrows(BadRequestException.class, () ->
                imageToPdfService.convertImagesToPdf(List.of(badFile), null, "A4", "AUTO", "FIT", "NONE")
        );

        MockMultipartFile spoofedFile = new MockMultipartFile("files", "test.png", "image/png", "Not an image structure".getBytes());
        assertThrows(BadRequestException.class, () ->
                imageToPdfService.convertImagesToPdf(List.of(spoofedFile), null, "A4", "AUTO", "FIT", "NONE")
        );
    }
}
