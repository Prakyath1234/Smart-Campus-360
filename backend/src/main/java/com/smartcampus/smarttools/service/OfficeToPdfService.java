package com.smartcampus.smarttools.service;

import com.smartcampus.smarttools.exception.DocumentConversionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class OfficeToPdfService {

    private final TemporaryFileService temporaryFileService;

    @Value("${smarttools.converter.enabled:true}")
    private boolean enabled;

    @Value("${smarttools.converter.libreoffice-path:}")
    private String libreOfficePath;

    @Value("${smarttools.converter.timeout-seconds:60}")
    private int timeoutSeconds;

    private String resolvedPath;

    public OfficeToPdfService(TemporaryFileService temporaryFileService) {
        this.temporaryFileService = temporaryFileService;
    }

    @PostConstruct
    public void init() {
        if (!enabled) {
            System.out.println("SmartTools OfficeToPdfConverter is disabled by configuration.");
            return;
        }

        String envOverride = System.getenv("SMARTTOOLS_LIBREOFFICE_PATH");
        if (envOverride != null && !envOverride.trim().isEmpty()) {
            libreOfficePath = envOverride;
        }

        if (libreOfficePath == null || libreOfficePath.trim().isEmpty()) {
            File std64 = new File("C:\\Program Files\\LibreOffice\\program\\soffice.exe");
            File std32 = new File("C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe");
            if (std64.exists()) {
                resolvedPath = std64.getAbsolutePath();
            } else if (std32.exists()) {
                resolvedPath = std32.getAbsolutePath();
            } else {
                resolvedPath = "soffice";
            }
        } else {
            resolvedPath = libreOfficePath;
        }

        System.out.println("SmartTools OfficeToPdf Resolved LibreOffice Path: " + resolvedPath);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getResolvedPath() {
        return resolvedPath;
    }

    public byte[] convertOfficeToPdf(MultipartFile file) {
        if (!enabled) {
            throw new DocumentConversionException("Document conversion is currently disabled on the server.");
        }

        if (resolvedPath == null || resolvedPath.trim().isEmpty()) {
            throw new DocumentConversionException("LibreOffice converter engine is not configured or missing on the server.");
        }

        File tempIn = null;
        File tempOutDir = null;
        File generatedPdf = null;

        try {
            tempIn = temporaryFileService.saveMultipartToTemp(file);

            Path outDirPath = Files.createTempDirectory("soffice-out-");
            tempOutDir = outDirPath.toFile();

            String profileUrl = new File(tempOutDir, "profile").toURI().toString();
            ProcessBuilder pb = new ProcessBuilder(
                    resolvedPath,
                    "-env:UserInstallation=" + profileUrl,
                    "--headless",
                    "--convert-to",
                    "pdf",
                    "--outdir",
                    tempOutDir.getAbsolutePath(),
                    tempIn.getAbsolutePath()
            );

            Process process = pb.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new DocumentConversionException("Document conversion timed out after " + timeoutSeconds + " seconds.");
            }

            if (process.exitValue() != 0) {
                throw new DocumentConversionException("LibreOffice converter process failed with exit code: " + process.exitValue());
            }

            String inputBaseName = tempIn.getName().substring(0, tempIn.getName().lastIndexOf("."));
            generatedPdf = new File(tempOutDir, inputBaseName + ".pdf");

            if (!generatedPdf.exists() || generatedPdf.length() == 0) {
                throw new DocumentConversionException("Generated PDF file is missing or empty.");
            }

            try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(generatedPdf)) {
                if (doc.getNumberOfPages() == 0) {
                    throw new DocumentConversionException("Generated PDF contains zero pages.");
                }
            } catch (IOException e) {
                throw new DocumentConversionException("Generated PDF structure is corrupted or invalid.", e);
            }

            return Files.readAllBytes(generatedPdf.toPath());

        } catch (IOException | InterruptedException e) {
            throw new DocumentConversionException("Error executing LibreOffice conversion process: " + e.getMessage(), e);
        } finally {
            if (tempIn != null) {
                temporaryFileService.cleanUpTempFile(tempIn);
            }
            if (generatedPdf != null) {
                temporaryFileService.cleanUpTempFile(generatedPdf);
            }
            if (tempOutDir != null && tempOutDir.exists()) {
                deleteDirectoryRecursively(tempOutDir);
            }
        }
    }

    private void deleteDirectoryRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectoryRecursively(child);
                }
            }
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            System.err.println("Warning: failed to delete temp path: " + file.getAbsolutePath());
        }
    }
}
