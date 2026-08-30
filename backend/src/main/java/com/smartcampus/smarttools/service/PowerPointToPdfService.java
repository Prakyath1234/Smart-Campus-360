package com.smartcampus.smarttools.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class PowerPointToPdfService {

    private final FileValidationService fileValidationService;
    private final OfficeToPdfService officeToPdfService;

    public PowerPointToPdfService(FileValidationService fileValidationService, OfficeToPdfService officeToPdfService) {
        this.fileValidationService = fileValidationService;
        this.officeToPdfService = officeToPdfService;
    }

    public byte[] convertPowerPointToPdf(MultipartFile file) {
        validatePowerPointFile(file);
        return officeToPdfService.convertOfficeToPdf(file);
    }

    private void validatePowerPointFile(MultipartFile file) {
        fileValidationService.validateFile(file);

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new com.smartcampus.exception.BadRequestException("Invalid filename.");
        }
        String lowerName = originalName.toLowerCase();
        if (!lowerName.endsWith(".ppt") && !lowerName.endsWith(".pptx")) {
            throw new com.smartcampus.exception.BadRequestException("Only PPT and PPTX PowerPoint presentations are supported.");
        }

        if (lowerName.endsWith(".pptx")) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry = zis.getNextEntry();
                boolean hasPresentation = false;
                while (entry != null) {
                    if (entry.getName().contains("presentation.xml")) {
                        hasPresentation = true;
                        break;
                    }
                    entry = zis.getNextEntry();
                }
                if (!hasPresentation) {
                    throw new com.smartcampus.exception.BadRequestException("The uploaded file is not a valid PPTX presentation structure.");
                }
            } catch (IOException e) {
                throw new com.smartcampus.exception.BadRequestException("The uploaded PPTX file is corrupted or unreadable.");
            }
        }
    }
}
