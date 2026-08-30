# Smart Campus 360 - Append-Only Audit Log System

## 1. Overview
The Audit Log System in **Smart Campus 360** provides immutable, append-only tracking of administrative and critical operational actions across the platform.

---

## 2. Tracked Actions

| Action Code | Description | Triggered By |
| :--- | :--- | :--- |
| `USER_CREATED` | New user account created | Admin / Onboarding |
| `USER_UPDATED` | Account profile/role modified | Admin |
| `USER_DISABLED` | Account soft deactivation | Admin |
| `STUDENT_CREATED` | Student profile registered | Admin |
| `FACULTY_CREATED` | Faculty profile registered | Admin |
| `MARKS_UPDATED` | Academic grade recorded | Faculty |
| `ATTENDANCE_UPDATED` | Attendance log updated | Faculty |
| `LEAVE_APPROVED` | Leave application approved | Faculty / Admin |
| `LEAVE_REJECTED` | Leave application rejected | Faculty / Admin |
| `SOS_TRIGGERED` | Distress signal dispatched | Student |
| `SOS_ACKNOWLEDGED` | Response unit assigned | Security |
| `SOS_RESOLVED` | Incident marked resolved | Security |
| `ANNOUNCEMENT_CREATED` | Broadcast announcement created | Admin / Faculty |

---

## 3. Querying Audit Logs
- **Endpoint**: `GET /api/admin/audit-logs`
- **Security**: Requires `ROLE_ADMIN`
- **Supported Parameters**: `actor`, `action`, `entityType`, `startDate`, `endDate`, `page`, `size`, `sortBy`, `direction`.
