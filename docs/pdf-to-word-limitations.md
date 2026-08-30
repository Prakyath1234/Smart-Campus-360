# PDF to Word Conversion Limitations

This document outlines the design boundaries, formatting limitations, and expected rendering behavior for the PDF to Word converter module.

---

## 1. Fixed-Layout vs Structured Documents
- **PDF Layout**: PDFs are designed as static, "electronic paper" layout formats. Elements (text strings, shapes, images) are placed at absolute (X, Y) coordinates. Paragraph flow, columns, and margins do not naturally exist.
- **DOCX Layout**: Word documents are structured XML documents that flow dynamically depending on page size, margins, and rendering engines.
- **Limitation**: Exact layout recreation is mathematically impossible. The converter's objective is to extract legible, editable text in a clean sequential hierarchy.

---

## 2. Text Extraction & Formatting
- **Font Extraction**: Font name metadata in PDFs is often compressed, subsetted, or proprietary. The converter attempts to extract readable font sizes and basic styles (Bold, Italic) based on character size offsets, falling back to clean standard serif/sans-serif sizes.
- **Complex Layouts**: Multi-column text layouts, text wrapping around circular shapes, or right-to-left text alignment may read out of order.

---

## 3. Image Extraction
- **Resolution**: Only embedded raster images (JPEG, PNG) can be extracted. Vector graphics, line drawings, and annotations are flattened and may be skipped.
- **Errors**: If image extraction on a specific page fails, the converter suppresses the exception and successfully continues exporting the surrounding text layout.

---

## 4. Tables
- **Detection**: Only simple grid tables with clear horizontal/vertical margins can be detected.
- **Limitation**: Nested cell contents, merged rows/columns, or boundary-less data grids will be extracted as linear text lines to prevent document corruption.

---

## 5. Scanned PDFs & OCR
- **Image-Only Rejection**: If the text stripper extracts fewer than 15 characters, the file is rejected with an error:
  > "This PDF appears to contain scanned/image-only pages. OCR is required to extract editable text."
- **No OCR Support**: Dynamic optical character recognition (OCR) is not supported in this phase.

---

## 6. Sizing Constraints
- **File Size**: Upload limit is enforced at 20 MB.
- **Page Limit**: Enforced at 100 pages per request via `smarttools.pdf-to-word.max-pages` config parameter.
