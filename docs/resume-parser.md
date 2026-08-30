# Resume Parser and Structured Resume Engine

This document provides architectural and technical documentation for the local deterministic Resume Parser.

---

## 1. Supported Formats
- **PDF**: Parses readable layouts using `Apache PDFBox` text streams.
- **DOCX**: Extracts structured paragraphs natively using `Apache POI`.
- **Validation**: Enforces type boundaries and size checks. Files lacking legible text (fewer than 25 characters) are rejected under scanned document checks.

---

## 2. Extraction Strategy
- **Text Stream Extraction**: Sequential line-by-line reading with space and carriage return normalization.
- **Section Segmentation**: Dynamic line analysis mapping contents into discrete section buckets:
  - `SUMMARY`, `EDUCATION`, `EXPERIENCE`, `SKILLS`, `PROJECTS`, `CERTIFICATIONS`, `ACHIEVEMENTS`.

---

## 3. Contact Detection Heuristics
- **Email**: Matching standard safe regex constraints.
- **Phone**: Matches common Indian (`+91`, `0`) and international telephone configurations.
- **Profiles**: Extracts LinkedIn profile page slugs, GitHub username links, and standalone portfolio domains.

---

## 4. Categorized Skills Dictionary
- **Languages**: `Java`, `Python`, `C++`, `C`, `Go`, `Kotlin`, `SQL`, `JavaScript`, `TypeScript`
- **Frameworks**: `Spring Boot`, `Spring`, `React`, `Angular`, `Node.js`, `Express`, `Hibernate`
- **Databases**: `MySQL`, `PostgreSQL`, `MongoDB`, `Oracle`, `Redis`
- **Cloud**: `AWS`, `Azure`, `GCP`
- **Tools**: `Git`, `GitHub`, `Docker`, `Kubernetes`, `Maven`, `Gradle`, `Jenkins`
- **Matching Rules**: Employs boundary matching regex structures (`\bJava\b`, `\bC\+\+\b`, etc.) to prevent false substring collisions.

---

## 5. Confidence Levels
- **Profile / Header**: `HIGH` if name and email are both extracted; `MEDIUM` otherwise.
- **Education / Experience / Skills**: `HIGH` if at least one entry/skill category is detected; `LOW` if empty.

---

## 6. Privacy & Lifecycles
- **No Persistence**: Resumes are parsed in-memory. Uploaded data is deleted immediately from JVM memory post-response. No documents are logged or saved to secondary storage.
