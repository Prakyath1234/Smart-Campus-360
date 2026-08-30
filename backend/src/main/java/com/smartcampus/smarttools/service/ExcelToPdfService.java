package com.smartcampus.smarttools.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ExcelToPdfService {

    private final FileValidationService fileValidationService;
    private final OfficeToPdfService officeToPdfService;

    public ExcelToPdfService(FileValidationService fileValidationService, OfficeToPdfService officeToPdfService) {
        this.fileValidationService = fileValidationService;
        this.officeToPdfService = officeToPdfService;
    }

    public byte[] convertExcelToPdf(MultipartFile file) {
        validateExcelFile(file);
        return officeToPdfService.convertOfficeToPdf(file);
    }

    private void validateExcelFile(MultipartFile file) {
        fileValidationService.validateFile(file);

        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new com.smartcampus.exception.BadRequestException("Invalid filename.");
        }
        String lowerName = originalName.toLowerCase();
        if (!lowerName.endsWith(".xls") && !lowerName.endsWith(".xlsx")) {
            throw new com.smartcampus.exception.BadRequestException("Only XLS and XLSX Excel spreadsheets are supported.");
        }

        if (lowerName.endsWith(".xlsx")) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry = zis.getNextEntry();
                boolean hasWorkbook = false;
                while (entry != null) {
                    if (entry.getName().contains("workbook.xml")) {
                        hasWorkbook = true;
                        break;
                    }
                    entry = zis.getNextEntry();
                }
                if (!hasWorkbook) {
                    throw new com.smartcampus.exception.BadRequestException("The uploaded file is not a valid XLSX spreadsheet structure.");
                }
            } catch (IOException e) {
                throw new com.smartcampus.exception.BadRequestException("The uploaded XLSX file is corrupted or unreadable.");
            }
        }
    }
}
