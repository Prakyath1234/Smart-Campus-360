package com.smartcampus.smarttools.service;

import com.smartcampus.smarttools.exception.DocumentProcessingException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageToPdfService {

    private final FileValidationService fileValidationService;
    private final TemporaryFileService temporaryFileService;

    @Value("${smarttools.image.max-count:20}")
    private int maxImageCount;

    public ImageToPdfService(FileValidationService fileValidationService, TemporaryFileService temporaryFileService) {
        this.fileValidationService = fileValidationService;
        this.temporaryFileService = temporaryFileService;
    }

    public byte[] convertImagesToPdf(List<MultipartFile> files, List<Integer> order,
                                     String pageSize, String orientation, String fitMode, String margins) {
        if (files == null || files.isEmpty()) {
            throw new com.smartcampus.exception.BadRequestException("Image files list cannot be empty.");
        }
        if (files.size() > maxImageCount) {
            throw new com.smartcampus.exception.BadRequestException("Exceeded maximum allowed files count of " + maxImageCount);
        }

        List<File> tempFiles = new ArrayList<>();
        List<BufferedImage> images = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : files) {
                fileValidationService.validateFile(multipartFile);

                String originalName = multipartFile.getOriginalFilename();
                if (originalName == null) {
                    throw new com.smartcampus.exception.BadRequestException("Invalid file name.");
                }
                String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
                if (!ext.equals("png") && !ext.equals("jpg") && !ext.equals("jpeg")) {
                    throw new com.smartcampus.exception.BadRequestException("Unsupported file type: " + ext);
                }

                BufferedImage img;
                try (InputStream is = multipartFile.getInputStream()) {
                    img = ImageIO.read(is);
                } catch (IOException e) {
                    throw new com.smartcampus.exception.BadRequestException("Failed to read image content. The file might be corrupted.");
                }
                if (img == null) {
                    throw new com.smartcampus.exception.BadRequestException("Unreadable or corrupted image format: " + originalName);
                }

                images.add(img);
            }

            List<BufferedImage> orderedImages = new ArrayList<>();
            if (order != null && !order.isEmpty()) {
                if (order.size() != images.size()) {
                    throw new com.smartcampus.exception.BadRequestException("Order sequence count must match the uploaded images count.");
                }
                boolean[] used = new boolean[images.size()];
                for (Integer index : order) {
                    if (index == null || index < 0 || index >= images.size()) {
                        throw new com.smartcampus.exception.BadRequestException("Invalid index in order list: " + index);
                    }
                    if (used[index]) {
                        throw new com.smartcampus.exception.BadRequestException("Duplicate index in order list: " + index);
                    }
                    used[index] = true;
                    orderedImages.add(images.get(index));
                }
            } else {
                orderedImages = images;
            }

            try (PDDocument doc = new PDDocument()) {
                for (BufferedImage img : orderedImages) {
                    PDRectangle rect;
                    if ("LETTER".equalsIgnoreCase(pageSize)) {
                        rect = PDRectangle.LETTER;
                    } else if ("ORIGINAL".equalsIgnoreCase(pageSize)) {
                        rect = new PDRectangle(img.getWidth(), img.getHeight());
                    } else {
                        rect = PDRectangle.A4;
                    }

                    boolean landscape = false;
                    if ("LANDSCAPE".equalsIgnoreCase(orientation)) {
                        landscape = true;
                    } else if ("AUTO".equalsIgnoreCase(orientation) || orientation == null) {
                        landscape = img.getWidth() > img.getHeight();
                    }

                    if (landscape && !"ORIGINAL".equalsIgnoreCase(pageSize)) {
                        rect = new PDRectangle(rect.getHeight(), rect.getWidth());
                    }

                    PDPage page = new PDPage(rect);
                    doc.addPage(page);

                    PDImageXObject pdImage = LosslessFactory.createFromImage(doc, img);

                    float marginWidth = 0f;
                    if ("SMALL".equalsIgnoreCase(margins)) {
                        marginWidth = 18f;
                    } else if ("MEDIUM".equalsIgnoreCase(margins)) {
                        marginWidth = 36f;
                    } else if ("LARGE".equalsIgnoreCase(margins)) {
                        marginWidth = 54f;
                    }

                    float pageWidth = page.getMediaBox().getWidth();
                    float pageHeight = page.getMediaBox().getHeight();

                    float contentWidth = pageWidth - (2 * marginWidth);
                    float contentHeight = pageHeight - (2 * marginWidth);

                    float imgWidth = pdImage.getWidth();
                    float imgHeight = pdImage.getHeight();

                    float drawWidth = contentWidth;
                    float drawHeight = contentHeight;

                    float x = marginWidth;
                    float y = marginWidth;

                    if ("FIT".equalsIgnoreCase(fitMode) || fitMode == null) {
                        float imgAspect = imgWidth / imgHeight;
                        float pageAspect = contentWidth / contentHeight;

                        if (imgAspect > pageAspect) {
                            drawWidth = contentWidth;
                            drawHeight = contentWidth / imgAspect;
                        } else {
                            drawHeight = contentHeight;
                            drawWidth = contentHeight * imgAspect;
                        }

                        x = marginWidth + (contentWidth - drawWidth) / 2f;
                        y = marginWidth + (contentHeight - drawHeight) / 2f;

                    } else if ("FILL".equalsIgnoreCase(fitMode)) {
                        float imgAspect = imgWidth / imgHeight;
                        float pageAspect = contentWidth / contentHeight;

                        if (imgAspect > pageAspect) {
                            drawHeight = contentHeight;
                            drawWidth = contentHeight * imgAspect;
                        } else {
                            drawWidth = contentWidth;
                            drawHeight = contentWidth / imgAspect;
                        }

                        x = marginWidth + (contentWidth - drawWidth) / 2f;
                        y = marginWidth + (contentHeight - drawHeight) / 2f;
                    }

                    try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                        contentStream.drawImage(pdImage, x, y, drawWidth, drawHeight);
                    }
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                doc.save(out);
                return out.toByteArray();
            }

        } catch (IOException e) {
            throw new DocumentProcessingException("Failed to generate PDF document from images.", e);
        } finally {
            for (File tempFile : tempFiles) {
                temporaryFileService.cleanUpTempFile(tempFile);
            }
        }
    }
}
