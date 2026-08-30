package com.smartcampus.smarttools;

import com.smartcampus.smarttools.exception.FileSizeLimitException;
import com.smartcampus.smarttools.exception.UnsupportedFileTypeException;
import com.smartcampus.smarttools.service.FileValidationService;
import com.smartcampus.smarttools.service.TemporaryFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class SmartToolsFoundationTest {

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private TemporaryFileService temporaryFileService;

    @Test
    void testFileValidationSuccess() {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", new byte[1024]
        );
        assertDoesNotThrow(() -> fileValidationService.validateFile(validFile));
    }

    @Test
    void testFileValidationUnsupportedType() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream", new byte[1024]
        );
        assertThrows(UnsupportedFileTypeException.class, () -> fileValidationService.validateFile(invalidFile));
    }

    @Test
    void testTemporaryFileCycle() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Sample Content".getBytes()
        );
        File temp = temporaryFileService.saveMultipartToTemp(file);
        assertNotNull(temp);
        assertTrue(temp.exists());
        assertTrue(temp.getName().endsWith(".docx"));

        temporaryFileService.cleanUpTempFile(temp);
        assertFalse(temp.exists());
    }
}
