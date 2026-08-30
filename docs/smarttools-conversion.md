# SmartTools Document Conversion Documentation

This document describes the design, setup, security architecture, and limitations of the Word to PDF converter module.

---

## Technical Design & Strategy
We use **LibreOffice in headless mode** (`soffice`) for Word-to-PDF conversion:
- Headless execution is safe, handles complex layouts, tables, images, and yields a real PDF output.
- Avoids fake text-only mock PDF generation.

---

## Configuration & Setup

### Configuration Properties
The module reads settings from [application.properties](file:///f:/360/Smart-Campus-360/backend/src/main/resources/application.properties):
- `smarttools.converter.enabled`: Enables or disables conversion endpoints.
- `smarttools.converter.libreoffice-path`: Direct path to `soffice` executable.
- `smarttools.converter.timeout-seconds`: Maximum execution duration (default `60` seconds) to prevent frozen processes.

### Windows Environment Setup
On startup, the system attempts to auto-detect the installation of LibreOffice:
1. Searches standard directories:
   - `C:\Program Files\LibreOffice\program\soffice.exe`
   - `C:\Program Files (x86)\LibreOffice\program\soffice.exe`
2. If not found, falls back to `soffice` (system PATH).
3. If LibreOffice is not installed, the converter service validates input structures but rejects execution with a configuration exception:
   > "LibreOffice converter engine is not configured or missing on the server."

---

## Timeout & Resource Management
- **Process Timeout**: Configured for 60 seconds. If `soffice` hangs, ProcessBuilder destroys the process forcibly (`process.destroyForcibly()`) and rolls back.
- **Immediate Disk Purge**: Input files, output files, and sub-directories created during the process are cleaned up inside `finally` blocks to guarantee no document residue is left on disk.

---

## Realistic Conversion Limitations
While LibreOffice does a great job, perfect document formatting is not guaranteed due to:
1. **Unsupported Custom Fonts**: System will fall back to default Sans-Serif or Serif fonts.
2. **Macros and Scripts**: Embedded VBA macros are ignored for safety reasons.
3. **Advanced Word Objects**: Dynamic SmartArt elements, certain math equations, or embedded legacy OLE objects may not render correctly.
4. **Proprietary Forms**: Form-fillable fields or active controls are converted as static text.
5. **PowerPoint Active Transitions**: Dynamic presentation animations, slide transitions, audio narration, and multimedia objects are flattened and converted as static, non-interactive PDF pages.
6. **One-to-One Slide Mapping**: Each slide in the PowerPoint input file (`.ppt` or `.pptx`) is converted as an individual page of the output PDF document.

---

## Standalone Image to PDF Conversion
Image-to-PDF conversion does NOT require LibreOffice:
- Implemented natively using Java's standard `ImageIO` and `Apache PDFBox`.
- Handled safely in-memory without starting background OS processes.
- Supports customizable output parameters: A4/Letter/Original sizes, portrait/landscape/auto detect layout options, aspect ratio fitting, and custom margins.

---

## Standalone PDF to Word Conversion
PDF-to-Word conversion is executed programmatically without LibreOffice:
- Uses `Apache PDFBox` to read text page by page, analyze basic layout (bold headings vs regular text), and extract embedded raster images.
- Reconstructs editable document formatting inside an `XWPFDocument` wrapper (using `Apache POI`).
- Restricts document execution to a maximum of 100 pages.
- Scanned PDF Detection: Automatically checks for extractable character length. If text length is under 15 characters, throws a controlled error indicating OCR is required.



