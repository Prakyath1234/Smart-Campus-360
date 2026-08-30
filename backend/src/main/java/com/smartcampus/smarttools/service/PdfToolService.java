package com.smartcampus.smarttools.service;

import com.smartcampus.smarttools.exception.DocumentProcessingException;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PdfToolService {

    private final FileValidationService fileValidationService;
    private final TemporaryFileService temporaryFileService;

    public PdfToolService(FileValidationService fileValidationService, TemporaryFileService temporaryFileService) {
        this.fileValidationService = fileValidationService;
        this.temporaryFileService = temporaryFileService;
    }

    private File validateAndSavePdf(MultipartFile file) {
        fileValidationService.validateFile(file);
        
        String originalName = file.getOriginalFilename();
        if (originalName != null && !originalName.toLowerCase().endsWith(".pdf")) {
            throw new com.smartcampus.exception.BadRequestException("Only PDF files are supported.");
        }

        File tempFile = temporaryFileService.saveMultipartToTemp(file);
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(tempFile)) {
            return tempFile;
        } catch (IOException e) {
            temporaryFileService.cleanUpTempFile(tempFile);
            throw new com.smartcampus.exception.BadRequestException("The uploaded file is not a valid PDF or is corrupted.");
        }
    }

    public byte[] mergePdfFiles(List<MultipartFile> files) {
        if (files == null || files.size() < 2) {
            throw new IllegalArgumentException("At least 2 PDF files are required for merging.");
        }

        List<File> tempFiles = new ArrayList<>();
        File mergedTempFile = null;

        try {
            PDFMergerUtility merger = new PDFMergerUtility();

            for (MultipartFile multipartFile : files) {
                File tempFile = validateAndSavePdf(multipartFile);
                tempFiles.add(tempFile);
                merger.addSource(tempFile);
            }

            String mergedName = UUID.randomUUID().toString() + ".pdf";
            mergedTempFile = new File(System.getProperty("java.io.tmpdir"), mergedName);
            merger.setDestinationFileName(mergedTempFile.getAbsolutePath());

            merger.mergeDocuments(null);

            return Files.readAllBytes(mergedTempFile.toPath());

        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to read or write PDF documents during merging", e);
        } finally {
            for (File tempFile : tempFiles) {
                temporaryFileService.cleanUpTempFile(tempFile);
            }
            if (mergedTempFile != null && mergedTempFile.exists()) {
                temporaryFileService.cleanUpTempFile(mergedTempFile);
            }
        }
    }

    public byte[] splitPdf(MultipartFile file, String splitMode, String rangesStr) {
        File tempFile = validateAndSavePdf(file);
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(tempFile)) {
            int maxPages = doc.getNumberOfPages();
            List<PageRange> ranges;
            if ("EVERY_PAGE".equalsIgnoreCase(splitMode)) {
                ranges = new ArrayList<>();
                for (int i = 1; i <= maxPages; i++) {
                    ranges.add(new PageRange(i, i));
                }
            } else if ("PAGE_RANGES".equalsIgnoreCase(splitMode)) {
                ranges = parsePageRanges(rangesStr, maxPages);
            } else {
                throw new com.smartcampus.exception.BadRequestException("Invalid split mode: " + splitMode);
            }

            java.io.ByteArrayOutputStream zipByteOut = new java.io.ByteArrayOutputStream();
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(zipByteOut)) {
                for (PageRange range : ranges) {
                    try (PDDocument splitDoc = new PDDocument()) {
                        for (int i = range.start; i <= range.end; i++) {
                            splitDoc.addPage(doc.getPage(i - 1));
                        }
                        String entryName = range.start == range.end ? 
                                "page-" + range.start + ".pdf" : 
                                "pages-" + range.start + "-" + range.end + ".pdf";
                        
                        entryName = entryName.replace("../", "").replace("..\\", "");

                        java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(entryName);
                        zos.putNextEntry(zipEntry);
                        splitDoc.save(zos);
                        zos.closeEntry();
                    }
                }
            }
            return zipByteOut.toByteArray();
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to split PDF document", e);
        } finally {
            temporaryFileService.cleanUpTempFile(tempFile);
        }
    }

    public byte[] extractPages(MultipartFile file, List<Integer> pagesToExtract) {
        if (pagesToExtract == null || pagesToExtract.isEmpty()) {
            throw new com.smartcampus.exception.BadRequestException("Extract page list cannot be empty.");
        }
        File tempFile = validateAndSavePdf(file);
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(tempFile)) {
            int maxPages = doc.getNumberOfPages();
            
            for (Integer page : pagesToExtract) {
                if (page == null || page <= 0 || page > maxPages) {
                    throw new com.smartcampus.exception.BadRequestException("Invalid page number for extraction: " + page);
                }
            }

            try (PDDocument extractDoc = new PDDocument()) {
                for (Integer page : pagesToExtract) {
                    extractDoc.addPage(doc.getPage(page - 1));
                }
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                extractDoc.save(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to extract pages from PDF", e);
        } finally {
            temporaryFileService.cleanUpTempFile(tempFile);
        }
    }

    public byte[] rotatePdf(MultipartFile file, int rotationDegree) {
        if (rotationDegree != 90 && rotationDegree != 180 && rotationDegree != 270) {
            throw new com.smartcampus.exception.BadRequestException("Invalid rotation degree. Supported values: 90, 180, 270");
        }
        File tempFile = validateAndSavePdf(file);
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(tempFile)) {
            for (PDPage page : doc.getPages()) {
                int currentRotation = page.getRotation();
                page.setRotation((currentRotation + rotationDegree) % 360);
            }
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to rotate PDF", e);
        } finally {
            temporaryFileService.cleanUpTempFile(tempFile);
        }
    }

    public byte[] reorderPdf(MultipartFile file, List<Integer> pageOrder) {
        if (pageOrder == null || pageOrder.isEmpty()) {
            throw new com.smartcampus.exception.BadRequestException("Page order list cannot be empty.");
        }
        File tempFile = validateAndSavePdf(file);
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(tempFile)) {
            int maxPages = doc.getNumberOfPages();
            if (pageOrder.size() != maxPages) {
                throw new com.smartcampus.exception.BadRequestException("Page order count (" + pageOrder.size() + ") must match document page count (" + maxPages + ")");
            }

            boolean[] verified = new boolean[maxPages + 1];
            for (Integer page : pageOrder) {
                if (page == null || page <= 0 || page > maxPages) {
                    throw new com.smartcampus.exception.BadRequestException("Invalid page number in reorder list: " + page);
                }
                if (verified[page]) {
                    throw new com.smartcampus.exception.BadRequestException("Duplicate page number in reorder list: " + page);
                }
                verified[page] = true;
            }

            for (int i = 1; i <= maxPages; i++) {
                if (!verified[i]) {
                    throw new com.smartcampus.exception.BadRequestException("Missing page " + i + " in reorder list.");
                }
            }

            try (PDDocument reorderDoc = new PDDocument()) {
                for (Integer page : pageOrder) {
                    reorderDoc.addPage(doc.getPage(page - 1));
                }
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                reorderDoc.save(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to reorder PDF pages", e);
        } finally {
            temporaryFileService.cleanUpTempFile(tempFile);
        }
    }

    private List<PageRange> parsePageRanges(String rangeStr, int maxPage) {
        List<PageRange> ranges = new ArrayList<>();
        if (rangeStr == null || rangeStr.trim().isEmpty()) {
            throw new com.smartcampus.exception.BadRequestException("Page range cannot be empty.");
        }
        String[] parts = rangeStr.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                String[] bounds = part.split("-");
                if (bounds.length != 2) {
                    throw new com.smartcampus.exception.BadRequestException("Invalid range format: " + part);
                }
                int start = parsePageNum(bounds[0], maxPage);
                int end = parsePageNum(bounds[1], maxPage);
                if (start > end) {
                    throw new com.smartcampus.exception.BadRequestException("Start page cannot be greater than end page: " + part);
                }
                ranges.add(new PageRange(start, end));
            } else {
                int page = parsePageNum(part, maxPage);
                ranges.add(new PageRange(page, page));
            }
        }
        
        boolean[] used = new boolean[maxPage + 1];
        for (PageRange r : ranges) {
            for (int i = r.start; i <= r.end; i++) {
                if (used[i]) {
                    throw new com.smartcampus.exception.BadRequestException("Overlapping page ranges are not allowed.");
                }
                used[i] = true;
            }
        }
        return ranges;
    }

    private int parsePageNum(String s, int maxPage) {
        try {
            int page = Integer.parseInt(s.trim());
            if (page <= 0) {
                throw new com.smartcampus.exception.BadRequestException("Page number must be positive.");
            }
            if (page > maxPage) {
                throw new com.smartcampus.exception.BadRequestException("Page number exceeds document page count of " + maxPage);
            }
            return page;
        } catch (NumberFormatException e) {
            throw new com.smartcampus.exception.BadRequestException("Invalid page number format: " + s);
        }
    }

    private static class PageRange {
        int start;
        int end;
        PageRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
