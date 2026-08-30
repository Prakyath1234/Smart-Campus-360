# SmartTools API Documentation

SmartTools provides secure web-based utilities for document manipulation. All endpoints reside under `/api/tools` and require authentication.

---

## PDF Manipulation Endpoints

### 1. Merge PDF
- **Method**: `POST`
- **URL**: `/api/tools/pdf/merge`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `files` (List of Multipart Files): A minimum of 2 PDF files.
- **File Limit**: 20 MB per file.
- **Response**: `application/pdf` binary stream.
- **Errors**:
  - `400 Bad Request`: If fewer than 2 files are sent, or if any file is not a valid PDF.

---

### 2. Split PDF
- **Method**: `POST`
- **URL**: `/api/tools/pdf/split`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input PDF file.
  - `splitMode` (String): Either `EVERY_PAGE` or `PAGE_RANGES`.
  - `ranges` (String, Optional): Pages or ranges to split. Required when `splitMode` is `PAGE_RANGES`. E.g., `1-3,5,8-10`.
- **Response**: `application/zip` containing the split PDF files.
- **Errors**:
  - `400 Bad Request`: If ranges are invalid, negative, out of document bounds, or overlapping.

---

### 3. Extract PDF Pages
- **Method**: `POST`
- **URL**: `/api/tools/pdf/extract`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input PDF file.
  - `pages` (List of Integers): Page numbers to extract. E.g., `1,3,5`.
- **Response**: `application/pdf` containing only the extracted pages.
- **Errors**:
  - `400 Bad Request`: If pages list is empty or references page numbers out of bounds.

---

### 4. Rotate PDF Pages
- **Method**: `POST`
- **URL**: `/api/tools/pdf/rotate`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input PDF file.
  - `rotation` (Integer): Supported values: `90`, `180`, `270`.
- **Response**: `application/pdf` with rotated pages.
- **Errors**:
  - `400 Bad Request`: If rotation value is anything other than 90, 180, or 270.

---

### 5. Reorder PDF Pages
- **Method**: `POST`
- **URL**: `/api/tools/pdf/reorder`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input PDF file.
  - `pageOrder` (List of Integers): Complete page sequence. E.g., `5,1,3,2,4`.
- **Response**: `application/pdf` with reordered pages.
- **Errors**:
  - `400 Bad Request`: If the page count does not match the document, contains duplicates, or references out-of-bound pages.

---

## Document Conversion Endpoints

### 1. Word to PDF
- **Method**: `POST`
- **URL**: `/api/tools/convert/word-to-pdf`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input Word document (`.doc` or `.docx`).
- **File Limit**: 20 MB.
- **Response**: `application/pdf` binary stream.
- **Errors**:
  - `400 Bad Request`: If file format is not supported or structure is corrupted.
  - `500 Internal Server Error`: If LibreOffice is not installed/configured on the host server or conversion process fails.
- **Conversion Limitations**:
  - Complex multi-column layouts, unsupported custom fonts, embedded macros, or proprietary formatting may not render perfectly.

---

### 2. PowerPoint to PDF
- **Method**: `POST`
- **URL**: `/api/tools/convert/powerpoint-to-pdf`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input PowerPoint presentation (`.ppt` or `.pptx`).
- **File Limit**: 20 MB.
- **Response**: `application/pdf` binary stream.
- **Errors**:
  - `400 Bad Request`: If file format is not supported or structure is corrupted.
  - `500 Internal Server Error`: If LibreOffice is not installed/configured on the host server or conversion process fails.
- **Conversion Limitations**:
  - Slide animations, active presentation transitions, macros, OLE embedded objects, and external hyperlinks are flattened as static elements.

---

### 3. Images to PDF
- **Method**: `POST`
- **URL**: `/api/tools/image/to-pdf`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `files` (List of Multipart Files): Uploaded images (`.png`, `.jpg`, `.jpeg`).
  - `order` (List of Integers, Optional): Order index sequence mapping.
  - `pageSize` (String, Optional): Options `A4`, `LETTER`, `ORIGINAL`. Default `A4`.
  - `orientation` (String, Optional): Options `PORTRAIT`, `LANDSCAPE`, `AUTO`. Default `AUTO`.
  - `fitMode` (String, Optional): Options `FIT`, `FILL`. Default `FIT`.
  - `margins` (String, Optional): Options `NONE`, `SMALL`, `MEDIUM`, `LARGE`. Default `NONE`.
- **File Limit**: 20 MB per file, max 20 files.
- **Response**: `application/pdf` binary stream.
- **Errors**:
  - `400 Bad Request`: If empty image list, unsupported files, or corrupted image data.

---

### 4. PDF to Word
- **Method**: `POST`
- **URL**: `/api/tools/convert/pdf-to-word`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input PDF file.
- **File Limit**: 20 MB, maximum 100 pages.
- **Response**: `application/vnd.openxmlformats-officedocument.wordprocessingml.document` binary stream.
- **Errors**:
  - `400 Bad Request`: If file is corrupted, empty, exceeds page limits, or is classified as scanned/image-only.

---

### 5. Resume Parser
- **Method**: `POST`
- **URL**: `/api/resume/parse`
- **Authentication**: Bearer JWT Token
- **Request Format**: `multipart/form-data`
- **Parameters**:
  - `file` (Multipart File): The input resume file (`.pdf` or `.docx`).
  - `includeRawText` (Boolean, Optional): Defaults to `false`.
- **Response**: `application/json` containing structured DTO output.
- **Errors**:
  - `400 Bad Request`: If file is empty, corrupted, has an unsupported format, or lacks extractable text (scanned PDF warning).





