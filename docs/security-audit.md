# Security and Quality Hardening Audit Report

This document records the security, performance, and reliability audit carried out on the SmartCampus 360 suite.

---

## 1. Authentication & JWT Controls
- **JWT Expiration & Validation**: All tokens are signed using a server-side non-guessable HS256 secret. Token expiration is verified on every request by `JwtAuthenticationFilter`. Modified, expired, or malformed tokens are immediately rejected.
- **Context Injection**: Exposes authenticated credentials via Spring Security context variables. No passwords or secrets are ever logged.

---

## 2. Role-Based Access Controls (RBAC) & IDOR Mitigation
We addressed a potential Indirect Object Reference (IDOR) gap where authenticated students could query `/api/student/{id}/...` endpoints of other students:
- **Student Access Control**: Added `validateStudentAccess(Long id, Principal principal)` and `validateUserAccess(Long userId, Principal principal)` checks in `StudentController.java`. If the current user has the `ROLE_STUDENT` authority, they are forbidden (403) from querying any ID other than their own.
- **Mentorship Isolation**: Added `verifyMentorMenteeRelationship(String facultyEmail, Long studentId)` validation check in `MentorshipService.java` so that faculty members cannot view notes or performance metrics of students who are not assigned to them as mentees.
- **Emergency Incident Isolation**: Added `verifySosAccess(Long id, String email)` checks in `EmergencyAlertService.java` to ensure students can only view or cancel emergency signals that they triggered.

---

## 3. CORS Hardening
- **Wildcard Restraints**: Replaced loose wildcard allow-origin configurations (`*` or allowed-origin patterns `*`) with credentials enabled, which represents a major security vulnerability.
- **Explicit Allowed Origins**: Limited CORS requests specifically to:
  - `http://localhost:5173` (development port)
  - `http://127.0.0.1:5173`
  - `http://localhost:8080` (production default port)
  - `http://127.0.0.1:8080`

---

## 4. File Upload Security & Traversal Protections
- **Filename Sanitization**: Original file names containing path traversal constructs (e.g. `../../evil.pdf`, `..\\..\\evil.pdf`, empty filenames, or special Unicode sequences) are neutralized during file uploads.
- **UUID Mapping**: Files are copied using UUID strings as names on disk inside a secure subdirectory. The engine validates that the target path does not escape the configured bounds (`!targetPath.startsWith(tempDir)`).

---

## 5. State Machine Transition Protections
Enforced a state transition pipeline for SOS emergencies inside `EmergencyAlertService.java`:
- Valid Transitions:
  - `ACTIVE` → `ACKNOWLEDGED` or `CANCELLED`
  - `ACKNOWLEDGED` → `IN_PROGRESS`, `RESOLVED`, or `CANCELLED`
  - `IN_PROGRESS` → `RESOLVED` or `CANCELLED`
- Finalized states (`RESOLVED` or `CANCELLED`) cannot be modified further.
