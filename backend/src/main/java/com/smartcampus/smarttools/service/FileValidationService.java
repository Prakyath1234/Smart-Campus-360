package com.smartcampus.smarttools.service;

import com.smartcampus.smarttools.exception.FileSizeLimitException;
import com.smartcampus.smarttools.exception.UnsupportedFileTypeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class FileValidationService {

    // Default max size: 20 MB (20,971,520 bytes)
    @Value("${smarttools.max-file-size-bytes:20971520}")
    private long maxFileSizeBytes;

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "png", "jpg", "jpeg", "txt", "csv"
    ));

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or missing.");
        }

        // Validate File Size
        if (file.getSize() > maxFileSizeBytes) {
            double maxSizeMb = (double) maxFileSizeBytes / (1024 * 1024);
            throw new FileSizeLimitException(String.format("File size exceeds the maximum allowed limit of %.1f MB", maxSizeMb));
        }

        // Validate File Extension / Type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new UnsupportedFileTypeException("Invalid file name structure.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedFileTypeException(String.format("File type '.%s' is not supported.", extension));
        }
    }
}
