package com.smartcampus.smarttools.service;

import com.smartcampus.exception.BadRequestException;
import com.smartcampus.smarttools.exception.DocumentProcessingException;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class PdfToWordService {

    private final FileValidationService fileValidationService;

    @Value("${smarttools.pdf-to-word.max-pages:100}")
    private int maxPages;

    public PdfToWordService(FileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }

    public byte[] convertPdfToWord(MultipartFile file) {
        fileValidationService.validateFile(file);

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF documents are supported for this tool.");
        }

        try (InputStream is = file.getInputStream();
             PDDocument pdfDoc = org.apache.pdfbox.Loader.loadPDF(is.readAllBytes())) {

            int pageCount = pdfDoc.getNumberOfPages();
            if (pageCount == 0) {
                throw new BadRequestException("The uploaded PDF is empty.");
            }
            if (pageCount > maxPages) {
                throw new BadRequestException("PDF page count of " + pageCount + " exceeds maximum allowed limit of " + maxPages);
            }

            // Scanned PDF Detection
            PDFTextStripper textStripper = new PDFTextStripper();
            String fullText = textStripper.getText(pdfDoc);
            if (fullText == null || fullText.trim().length() < 15) {
                throw new BadRequestException("This PDF appears to contain scanned/image-only pages. OCR is required to extract editable text.");
            }

            // Document Reconstruction
            try (XWPFDocument docx = new XWPFDocument()) {
                for (int i = 0; i < pageCount; i++) {
                    PDPage page = pdfDoc.getPage(i);

                    // Extract and add text page by page
                    PDFTextStripper pageStripper = new PDFTextStripper();
                    pageStripper.setStartPage(i + 1);
                    pageStripper.setEndPage(i + 1);
                    String pageText = pageStripper.getText(pdfDoc);

                    if (pageText != null && !pageText.trim().isEmpty()) {
                        String[] lines = pageText.split("\\r?\\n");
                        for (String line : lines) {
                            if (line.trim().isEmpty()) continue;
                            XWPFParagraph p = docx.createParagraph();
                            XWPFRun r = p.createRun();
                            r.setText(line);

                            // Simple heading layout analysis
                            if (line.trim().length() < 60 && (line.trim().equals(line.toUpperCase()) || line.trim().startsWith("Heading") || line.trim().startsWith("Title"))) {
                                r.setBold(true);
                                r.setFontSize(14);
                            } else {
                                r.setFontSize(11);
                            }
                        }
                    }

                    // Attempt Image Extraction per page
                    try {
                        PDResources resources = page.getResources();
                        for (COSName name : resources.getXObjectNames()) {
                            PDXObject xobject = resources.getXObject(name);
                            if (xobject instanceof PDImageXObject) {
                                PDImageXObject pdImage = (PDImageXObject) xobject;
                                BufferedImage bufferedImage = pdImage.getImage();

                                ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
                                javax.imageio.ImageIO.write(bufferedImage, "png", imageBytes);

                                XWPFParagraph imageParagraph = docx.createParagraph();
                                XWPFRun imageRun = imageParagraph.createRun();
                                imageRun.addPicture(
                                        new ByteArrayInputStream(imageBytes.toByteArray()),
                                        XWPFDocument.PICTURE_TYPE_PNG,
                                        "extracted_image.png",
                                        Units.toEMU(200),
                                        Units.toEMU(200)
                                );
                            }
                        }
                    } catch (Exception imgEx) {
                        // Suppress image errors and proceed to satisfy standard limitations clause
                        System.err.println("Warning: failed to extract image from page " + (i + 1) + ": " + imgEx.getMessage());
                    }
                }

                // Output Validation
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                docx.write(out);
                byte[] docxBytes = out.toByteArray();

                // Reopen and validate generated DOCX
                try (XWPFDocument checkDoc = new XWPFDocument(new ByteArrayInputStream(docxBytes))) {
                    if (checkDoc.getParagraphs().isEmpty()) {
                        throw new DocumentProcessingException("Generated DOCX has no paragraphs.");
                    }
                } catch (IOException e) {
                    throw new DocumentProcessingException("Generated DOCX structure is corrupted.", e);
                }

                return docxBytes;
            }

        } catch (IOException e) {
            throw new DocumentProcessingException("Error processing PDF document: " + e.getMessage(), e);
        }
    }
}
