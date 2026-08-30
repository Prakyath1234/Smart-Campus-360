# Smart Campus 360 - Automated Testing Strategy

## 1. Test Suite Architecture

```mermaid
graph TD
    TestRunner["JUnit 5 Test Runner"] --> UnitTests["Unit & Integration Tests"]
    UnitTests --> AdminTest["AdminCrudTest (6 Tests)"]
    UnitTests --> CoreTest["SmartCampusApplicationTests (3 Tests)"]
    AdminTest --> UserCrud["User CRUD & Duplicate Validation"]
    AdminTest --> StudentCrud["Student Roll Number Validation"]
    AdminTest --> Mentorship["Mentorship & Analytics Evaluation"]
    AdminTest --> SOSState["SOS State Machine & Audit Logs"]
```

---

## 2. Test Coverage Summary

- **Authentication & Security Tests**: Verify registration, password hashing, and token issuance.
- **Validation & Duplicate Prevention Tests**: Verify HTTP 400 responses for duplicate roll numbers, emails, and invalid parameters.
- **Timetable Conflict Tests**: Verify classroom and semester time slot overlap detection.
- **Emergency State Machine Tests**: Verify valid transitions (`ACTIVE` → `ACKNOWLEDGED` → `RESOLVED`) and rejection of invalid state reverts (`RESOLVED` → `ACTIVE`).
- **Audit Logging Tests**: Verify append-only log creation during operations.
