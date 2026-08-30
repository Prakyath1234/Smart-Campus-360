package com.smartcampus.smarttools.service;

import com.smartcampus.smarttools.exception.DocumentProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TemporaryFileService {

    private final Path tempDir;

    public TemporaryFileService() {
        try {
            // Create a secure tools subdirectory within java temp folder
            this.tempDir = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir"), "smarttools-secure"));
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to initialize secure temporary file directory", e);
        }
    }

    public File saveMultipartToTemp(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Cannot save an empty file to temporary storage.");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate a safe, non-guessable random UUID filename to prevent path traversal and execution
        String safeName = UUID.randomUUID().toString() + extension;
        Path targetPath = tempDir.resolve(safeName).normalize();

        // Extra path traversal sanity check
        if (!targetPath.startsWith(tempDir)) {
            throw new DocumentProcessingException("Path traversal attempt detected!");
        }

        try {
            Files.copy(multipartFile.getInputStream(), targetPath);
            return targetPath.toFile();
        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to copy file to temporary disk storage", e);
        }
    }

    public void cleanUpTempFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                // Log warning and proceed (do not throw during cleanup)
                System.err.println("Warning: Failed to delete temporary file: " + file.getAbsolutePath());
            }
        }
    }
}
