package com.smartcampus.smarttools.controller;

import com.smartcampus.smarttools.service.PdfToolService;
import com.smartcampus.smarttools.service.WordToPdfService;
import com.smartcampus.smarttools.service.PowerPointToPdfService;
import com.smartcampus.smarttools.service.ExcelToPdfService;
import com.smartcampus.smarttools.service.ImageToPdfService;
import com.smartcampus.smarttools.service.PdfToWordService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tools")
@CrossOrigin(origins = "*")
public class SmartToolsController {

    private final PdfToolService pdfToolService;
    private final WordToPdfService wordToPdfService;
    private final PowerPointToPdfService powerPointToPdfService;
    private final ExcelToPdfService excelToPdfService;
    private final ImageToPdfService imageToPdfService;
    private final PdfToWordService pdfToWordService;

    public SmartToolsController(PdfToolService pdfToolService, WordToPdfService wordToPdfService,
                                PowerPointToPdfService powerPointToPdfService, ExcelToPdfService excelToPdfService,
                                ImageToPdfService imageToPdfService, PdfToWordService pdfToWordService) {
        this.pdfToolService = pdfToolService;
        this.wordToPdfService = wordToPdfService;
        this.powerPointToPdfService = powerPointToPdfService;
        this.excelToPdfService = excelToPdfService;
        this.imageToPdfService = imageToPdfService;
        this.pdfToWordService = pdfToWordService;
    }

    @PostMapping(value = "/pdf/merge", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> mergePdfs(@RequestParam("files") List<MultipartFile> files) {
        byte[] mergedBytes = pdfToolService.mergePdfFiles(files);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("merged.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(mergedBytes);
    }

    @PostMapping(value = "/pdf/split", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("splitMode") String splitMode,
            @RequestParam(value = "ranges", required = false) String ranges
    ) {
        byte[] zipBytes = pdfToolService.splitPdf(file, splitMode, ranges);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/zip"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("split-pages.zip").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipBytes);
    }

    @PostMapping(value = "/pdf/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> extractPages(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pages") List<Integer> pages
    ) {
        byte[] pdfBytes = pdfToolService.extractPages(file, pages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("extracted.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/pdf/rotate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> rotatePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("rotation") int rotation
    ) {
        byte[] pdfBytes = pdfToolService.rotatePdf(file, rotation);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("rotated.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/pdf/reorder", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> reorderPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageOrder") List<Integer> pageOrder
    ) {
        byte[] pdfBytes = pdfToolService.reorderPdf(file, pageOrder);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("reordered.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/convert/word-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertWordToPdf(@RequestParam("file") MultipartFile file) {
        byte[] pdfBytes = wordToPdfService.convertWordToPdf(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("converted.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/convert/powerpoint-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertPowerPointToPdf(@RequestParam("file") MultipartFile file) {
        byte[] pdfBytes = powerPointToPdfService.convertPowerPointToPdf(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("presentation.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/convert/excel-to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertExcelToPdf(@RequestParam("file") MultipartFile file) {
        byte[] pdfBytes = excelToPdfService.convertExcelToPdf(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("spreadsheet.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/image/to-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertImagesToPdf(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "order", required = false) List<Integer> order,
            @RequestParam(value = "pageSize", defaultValue = "A4") String pageSize,
            @RequestParam(value = "orientation", defaultValue = "AUTO") String orientation,
            @RequestParam(value = "fitMode", defaultValue = "FIT") String fitMode,
            @RequestParam(value = "margins", defaultValue = "NONE") String margins
    ) {
        byte[] pdfBytes = imageToPdfService.convertImagesToPdf(files, order, pageSize, orientation, fitMode, margins);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("images-to-pdf.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping(value = "/convert/pdf-to-word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> convertPdfToWord(@RequestParam("file") MultipartFile file) {
        byte[] docxBytes = pdfToWordService.convertPdfToWord(file);

        String originalFilename = file.getOriginalFilename();
        String outputFilename = "converted-document.docx";
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".pdf")) {
            outputFilename = originalFilename.substring(0, originalFilename.length() - 4) + ".docx";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(outputFilename).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(docxBytes);
    }
}
