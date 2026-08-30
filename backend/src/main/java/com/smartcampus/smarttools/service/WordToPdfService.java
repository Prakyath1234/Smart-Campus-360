package com.smartcampus.smarttools.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class WordToPdfService {

    private final FileValidationService fileValidationService;
    private final OfficeToPdfService officeToPdfService;

    public WordToPdfService(FileValidationService fileValidationService, OfficeToPdfService officeToPdfService) {
        this.fileValidationService = fileValidationService;
        this.officeToPdfService = officeToPdfService;
    }

    public byte[] convertWordToPdf(MultipartFile file) {
        validateWordFile(file);
        return officeToPdfService.convertOfficeToPdf(file);
    }

    private void validateWordFile(MultipartFile file) {
        fileValidationService.validateFile(file);

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new com.smartcampus.exception.BadRequestException("Invalid filename.");
        }
        String lowerName = originalName.toLowerCase();
        if (!lowerName.endsWith(".doc") && !lowerName.endsWith(".docx")) {
            throw new com.smartcampus.exception.BadRequestException("Only DOC and DOCX Word documents are supported.");
        }

        if (lowerName.endsWith(".docx")) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry = zis.getNextEntry();
                boolean hasContentTypes = false;
                while (entry != null) {
                    if ("[Content_Types].xml".equals(entry.getName())) {
                        hasContentTypes = true;
                        break;
                    }
                    entry = zis.getNextEntry();
                }
                if (!hasContentTypes) {
                    throw new com.smartcampus.exception.BadRequestException("The uploaded file is not a valid DOCX document structure.");
                }
            } catch (IOException e) {
                throw new com.smartcampus.exception.BadRequestException("The uploaded DOCX file is corrupted or unreadable.");
            }
        }
    }
}
