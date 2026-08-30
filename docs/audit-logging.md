# Lightweight Security Audit Logging Engine

This document details the schema, events, APIs, and administrative controls implemented for system audit tracking.

---

## 1. Events Logged
The audit engine records important operations, identifying who did what and when:
* `LOGIN`
* `USER_CREATED`
* `USER_UPDATED`
* `USER_DISABLED`
* `STUDENT_CREATED`
* `MARKS_UPDATED`
* `ATTENDANCE_UPDATED`
* `LEAVE_APPROVED`
* `LEAVE_REJECTED`
* `COMPLAINT_UPDATED`
* `SERVICE_REQUEST_UPDATED`
* `EMERGENCY_TRIGGERED`
* `EMERGENCY_ACKNOWLEDGED`
* `EMERGENCY_RESOLVED`
* `MENTOR_ASSIGNED`
* `ANNOUNCEMENT_CREATED`

No sensitive payloads (such as passwords, JWT keys, or personal phone numbers) are ever persisted in the audit logs database.

---

## 2. Database Schema (`AuditLog` entity)

| Field | Type | Description |
|---|---|---|
| **id** | Long | Primary Key (auto-incremented) |
| **actor** | String | Email or identity of the user initiating the action |
| **actorRole** | String | Authority group of the actor (e.g. `ADMIN`, `SECURITY`) |
| **action** | String | Operation type (e.g. `SOS_TRIGGERED`) |
| **entityType** | String | Name of the impacted database entity (e.g. `EmergencyAlert`) |
| **entityId** | Long | Database identifier of the impacted object |
| **description** | String | Structured message detailing parameters and status |
| **timestamp** | LocalDateTime | Date and time when the event was recorded |

---

## 3. Administration REST API
Exposes the logs via pageable endpoints restricted to the **ADMIN** role.

### `GET /api/admin/audit-logs`
* **Authority**: `PreAuthorize("hasAuthority('ROLE_ADMIN')")`
* **Parameters**:
  - `actor` (optional string): Filter by user email.
  - `action` (optional string): Filter by action name.
  - `entityType` (optional string): Filter by entity class name.
  - `page` (default 0): Page number.
  - `size` (default 15): Page size.
* **Response**: A standard Spring `Page` object containing audit log records.

---

## 4. Frontend View Panel
Integrated a **Security Audit Logs** dashboard tab inside the Admin Dashboard:
- Filters logs by user actor, action keywords, and entity types.
- Paginated results using simple `Next` and `Previous` buttons.
- Displayed in a tabular format showing Timestamp, Actor, Role, Action, Entity, Entity ID, and Details.
