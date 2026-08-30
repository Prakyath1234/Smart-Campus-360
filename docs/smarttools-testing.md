# SmartTools Testing Documentation

Testing verifies the correct behavior, safety constraints, resource cleanup, and regression status of the SmartTools utility module.

---

## Backend Automated Integration Tests
Location: [PdfToolTest.java](file:///f:/360/Smart-Campus-360/backend/src/test/java/com/smartcampus/smarttools/PdfToolTest.java)

The test suite runs using in-memory H2 DB environment and mocks multi-page PDF documents.

### Test Cases Covered

| Test Category | Test Case Method | Description | Expected Result | Status |
|---|---|---|---|---|
| **Merge PDF** | `testMergePdfFilesSuccess` | Merges two separate 1-page PDF documents. | 2-page merged PDF returned. | **PASSED** |
| **Split PDF** | `testSplitPdfEveryPage` | Splits a 3-page PDF into single pages. | ZIP containing 3 separate PDF files. | **PASSED** |
| **Split PDF** | `testSplitPdfPageRanges` | Splits a 10-page PDF with range `1-3,5,8-10`. | ZIP containing 3 segmented PDFs. | **PASSED** |
| **Split PDF** | `testSplitPdfInvalidRanges` | Submits ranges with page zero, negative bounds, overlapping regions, and bounds out of range. | `BadRequestException` (HTTP 400). | **PASSED** |
| **Extract Pages** | `testExtractPagesSuccess` | Extracts pages 1, 3, and 5 from a 5-page PDF. | 3-page extracted PDF document. | **PASSED** |
| **Extract Pages** | `testExtractPagesInvalidInputs` | Submits empty extract lists, out-of-range bounds, or negative bounds. | `BadRequestException` (HTTP 400). | **PASSED** |
| **Rotate PDF** | `testRotatePdfSuccess` | Rotates a document by 90 degrees. | Rotation metadata updated to 90 degrees on all pages. | **PASSED** |
| **Rotate PDF** | `testRotatePdfInvalidDegree` | Submits 45 or -90 degree rotation. | `BadRequestException` (HTTP 400). | **PASSED** |
| **Reorder PDF** | `testReorderPdfSuccess` | Reorders pages in sequence `5,1,3,2,4`. | Reordered 5-page PDF document. | **PASSED** |
| **Reorder PDF** | `testReorderPdfInvalidInputs` | Submits missing pages, duplicates, or out-of-range pages in sequence. | `BadRequestException` (HTTP 400). | **PASSED** |
| **Security** | `testSecurityMalformedPdfRejected` | Submits raw non-PDF byte stream renamed as `.pdf`. | `BadRequestException` (HTTP 400). | **PASSED** |
| **Word PDF** | `testRealWordToPdfConversionSuccess` | Converts a programmatically generated 2-page DOCX document. | Valid 2-page PDF document returned. | **PASSED** |
| **PPTX PDF** | `testRealPowerPointToPdfConversionSuccess` | Converts a programmatically generated 3-slide PPTX presentation. | Valid 3-page PDF document returned. | **PASSED** |
| **XLSX PDF** | `testRealExcelToPdfConversionSuccess` | Converts a programmatically generated 3-sheet XLSX spreadsheet. | Valid PDF document returned. | **PASSED** |
| **Image PDF** | `testSinglePngConversion` | Converts a tall portrait PNG image. | Valid 1-page PDF document returned. | **PASSED** |
| **Image PDF** | `testSingleJpgConversion` | Converts a wide landscape JPG image. | Valid 1-page PDF document returned. | **PASSED** |
| **Image PDF** | `testMultipleImagesAndCustomOrder` | Converts multiple images using custom ordering indexes. | Valid 2-page PDF document returned. | **PASSED** |
| **PDF Word** | `testRealPdfToWordConversionSuccess` | Converts a text PDF to Word, verifying character retention percentage. | Valid DOCX document containing original text. | **PASSED** |
| **PDF Word** | `testScannedPdfDetection` | Submits a PDF containing an image but no text. | Rejects with BadRequestException. | **PASSED** |

---

## Execution Command
To execute the tests locally:
```powershell
mvn clean test
```
Result: **`BUILD SUCCESS`** (45 tests passed successfully).


