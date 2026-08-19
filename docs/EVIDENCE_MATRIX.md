# Internship Evidence Matrix

## Bank Access Management System

This document maps the main internship task areas to concrete implementation evidence in the Bank Access Management System project.

The goal is to provide a concise reference for project review, internship reporting and final demonstration.

---

## 1. Development Environment and Project Setup

| Requirement / Skill | Implementation | Evidence | Demo / Verification |
|---|---|---|---|
| Java development environment | Backend implemented with Java 21 and Spring Boot | `backend/pom.xml` | `.\mvnw.cmd test` |
| Frontend development environment | React + TypeScript + Vite frontend | `frontend/package.json` | `npm.cmd run build` |
| PostgreSQL environment | PostgreSQL 17 running through Docker Compose | `docker/compose.yml` | `docker ps` |
| Environment configuration | Local DB credentials and JWT configuration separated from source control | `docker/.env.example`, `.gitignore`, `application-local.yaml` locally | Start backend with `local` profile |
| Version control | Git repository with feature, fix and documentation branches | Git history / GitHub | Show merged pull requests |
| Project documentation | Full setup, architecture, security and demo documentation | `README.md` | GitHub repository root |

**Status:** Completed.

---

## 2. Frontend Development

### User Management

Implemented:

- user list
- user search
- create user
- edit user
- account status management
- user deactivation
- administrator protection
- automatic table refresh after operations

Evidence:

- `frontend/src/UsersPage.tsx`
- PR #1 — `feat: add frontend user management`

Verification:

- user creation tested in the UI
- user editing tested
- deactivation tested
- protected administrator behavior tested

---

### Department Management

Implemented:

- department list
- search
- create department
- edit department
- parent department selection
- hierarchy support
- department deactivation
- hierarchy cycle protection

Evidence:

- `frontend/src/DepartmentsPage.tsx`
- PR #2 — `feat: add frontend department management`

Verification:

- department creation tested
- parent-child relation tested
- editing tested
- deactivation tested

---

### Role Management

Implemented:

- role list
- search
- role creation
- editing
- activation / deactivation
- system-role protection
- user-role assignment
- optional role expiration
- role revocation

Evidence:

- `frontend/src/RolesPage.tsx`
- user-role assignment modal/components
- PR #3 — `feat: add frontend role management and assignments`

Verification:

- custom role lifecycle tested
- `RISK_DEMO_ROLE` used during live verification
- user role assignment tested
- role revocation tested
- system-role protection tested

---

### Permission Management

Implemented:

- permission list
- search
- permission creation
- editing
- activation / deactivation
- system-permission protection

Evidence:

- `frontend/src/PermissionsPage.tsx`
- PR #4 — `feat: add frontend permission management`

Verification:

- custom permission lifecycle tested
- `RISK_REPORT_EXPORT` used during live verification
- protected system permissions verified

---

### Audit Log Interface

Implemented:

- audit log table
- search
- manual refresh
- actor information
- action and result display
- entity information
- IP address
- event timestamp
- structured JSON details

Evidence:

- `frontend/src/AuditPage.tsx`
- PR #5 — `feat: add frontend audit log`

Verification:

- real audit records loaded from backend
- JSON event details displayed
- logout audit events verified

---

### Logout Integration

Implemented:

- frontend calls backend logout API
- current refresh token is sent for revocation
- local authentication state is cleared
- logout remains resilient if backend request fails
- duplicate actions prevented while request is pending

Evidence:

- authentication/frontend session code
- PR #6 — `feat: add backend logout integration`

Verification:

```text
POST /api/auth/logout -> 204
```

Successful logout is visible in the audit log.

---

### Dashboard

Implemented:

- live user count
- live role count
- live permission count
- live audit event count
- quick navigation to administration modules

Evidence:

- dashboard frontend code
- PR #7 — `feat: add dashboard statistics`

Live verification included:

```text
5 users
5 roles
23 permissions
18 audit events
```

The values are loaded from backend APIs rather than hard-coded placeholders.

---

### Role-Permission Management

Implemented:

- view permissions assigned to a role
- view available active permissions
- grant permission
- revoke permission
- protection for system roles
- inactive-role restrictions
- read-only system-role mode

Evidence:

- `frontend/src/RolePermissionsModal.tsx`
- `frontend/src/RolesPage.tsx`
- PR #8 — `feat: add role permission management`

Verification:

- `PERMISSION_VIEW` granted to `RISK_DEMO_ROLE`
- permission successfully revoked
- `ADMIN` system role displayed in read-only mode
- revoke controls disabled for the system role

**Frontend status:** Completed.

---

## 3. Backend Java and REST API Development

The backend is implemented with Java 21 and Spring Boot.

Main modules include:

```text
Authentication
Users
Departments
Roles
Permissions
User Roles
Role Permissions
Audit Logs
```

Representative API groups:

```text
/api/auth
/api/users
/api/departments
/api/roles
/api/permissions
/api/audit-logs
```

RBAC relationship endpoints include:

```text
/api/users/{userId}/roles
/api/users/{userId}/effective-permissions
/api/roles/{roleId}/permissions
/api/permissions/{permissionId}/roles
```

Backend architecture uses:

- controllers
- services
- repositories
- DTO/request validation
- JPA entities
- Spring Security
- PostgreSQL persistence
- transaction boundaries

Evidence:

```text
backend/src/main/java/com/timurtokaev/bankaccess/
```

**Status:** Completed.

---

## 4. PostgreSQL, SQL and Database Design

Database:

```text
PostgreSQL 17
```

Database migrations are managed by Flyway.

Migration files:

```text
backend/src/main/resources/db/migration/
├── V1__create_initial_schema.sql
├── V2__seed_system_roles_and_permissions.sql
├── V3__protect_system_permissions.sql
└── V4__invalidate_account_sessions.sql
```

### Core Tables

Implemented relational tables include:

```text
departments
users
roles
permissions
user_roles
role_permissions
refresh_tokens
audit_logs
```

The schema uses:

- UUID primary keys
- foreign keys
- unique constraints
- check constraints
- many-to-many RBAC relationships
- self-referencing department hierarchy
- JSONB audit details
- PostgreSQL `INET` fields
- timestamps
- token expiration and revocation data

### SQL Indexes

Indexes implemented in the initial schema include:

```text
idx_departments_parent_id
idx_users_department_id
idx_users_status
idx_user_roles_role_id
idx_user_roles_assigned_by
idx_role_permissions_permission_id
idx_refresh_tokens_user_id
idx_refresh_tokens_expires_at
idx_audit_logs_actor_user_id
idx_audit_logs_occurred_at
idx_audit_logs_entity_type_entity_id
idx_audit_logs_action
```

These support common relationship lookups, account filtering, refresh-token operations and audit-log queries.

Evidence:

```text
backend/src/main/resources/db/migration/V1__create_initial_schema.sql
```

Flyway verification during integration tests:

```text
Successfully validated 4 migrations
Current version of schema "public": 4
Schema "public" is up to date
```

**Status:** Completed.

---

## 5. Git, Branches, Commits and Pull Requests

Development followed a branch-based Git workflow.

Merged frontend pull requests:

| PR | Feature |
|---|---|
| #1 | User management |
| #2 | Department management |
| #3 | Role management and user-role assignments |
| #4 | Permission management |
| #5 | Audit log |
| #6 | Backend logout integration |
| #7 | Dashboard statistics |
| #8 | Role-permission management |
| #9 | Logout audit integration test profile fix |
| #10 | Final project README |

Examples of branches:

```text
feature/frontend-user-management
feature/frontend-departments
feature/frontend-roles
feature/frontend-permissions
feature/frontend-audit
feature/frontend-logout
feature/frontend-dashboard-stats
feature/frontend-role-permissions
fix/logout-audit-test-profile
docs/final-readme
docs/evidence-matrix
```

Workflow demonstrated:

```text
main
  ↓
feature/fix/docs branch
  ↓
implementation
  ↓
build / test
  ↓
git commit
  ↓
git push
  ↓
Pull Request
  ↓
verification
  ↓
merge into main
```

**Status:** Completed.

---

## 6. Security, Validation and OWASP-Related Practices

The project includes multiple application-security controls.

### Password Security

Passwords are stored using BCrypt hashing rather than plaintext values.

### JWT Authentication

Protected API endpoints use JWT access tokens.

Access token configuration includes:

- issuer
- audience
- expiration
- signed JWT secret

### Refresh Tokens

Refresh tokens:

- are persisted in PostgreSQL
- have expiration timestamps
- can be revoked
- are revoked during logout
- are invalidated when account/session state requires it

### Login Protection

The authentication system includes:

- failed login attempt tracking
- temporary account locking
- configurable maximum failed attempts
- configurable lock duration

### Role-Based Access Control

Authorization uses:

```text
User
  ↓
User Role
  ↓
Role
  ↓
Role Permission
  ↓
Permission
```

Backend endpoint access is permission-based.

Examples include:

```text
PERMISSION_VIEW
PERMISSION_GRANT
PERMISSION_REVOKE
AUDIT_LOG_VIEW
```

### Delegation Policy

The backend applies authorization rules when users grant or revoke access.

This prevents relying only on frontend restrictions.

### System Object Protection

System roles and system permissions have additional protection against unsafe modification.

### Input Validation

Backend request objects use server-side validation.

Validation occurs before sensitive business operations.

### Audit Logging

Security-relevant operations are written to audit records.

The logout implementation records a successful audit event after refresh-token revocation.

### Security Principle

Frontend controls improve usability, but backend authorization remains the security boundary.

**Status:** Completed.

---

## 7. Automated Testing

Backend tests use technologies including:

- JUnit
- Mockito
- Spring Boot Test
- MockMvc
- Spring Security Test
- PostgreSQL-backed integration tests

Representative test classes include:

```text
AuditLogControllerTest
AccessTokenServiceTest
AccountSessionInvalidationIntegrationTest
AuthControllerTest
JwtConfigTest
LoginStateServiceTest
LogoutAuditIntegrationTest
RefreshTokenServiceTest
TransactionalLoginServiceTest
TransactionalLogoutServiceTest
TransactionalRefreshServiceTest
UserAccessTokenValidatorTest
BankAccessManagementBackendApplicationTests
SecurityConfigTest
PermissionServiceTest
RolePermissionServiceTest
UserServiceTest
DelegationPolicyTest
UserRoleServiceTest
```

Final verified backend test result:

```text
Tests run: 80
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

The logout audit integration test was also verified independently:

```text
Tests run: 1
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Evidence:

- backend test source directory
- PR #9 — `test: use local profile for logout audit integration`

**Status:** Completed.

---

## 8. Frontend Build Verification

Frontend production build command:

```powershell
npm.cmd run build
```

The command performs:

```text
TypeScript compilation
+
Vite production build
```

Final verified result:

```text
✓ 23 modules transformed
✓ production build completed successfully
```

Evidence:

```text
frontend/package.json
```

**Status:** Completed.

---

## 9. Documentation

Project documentation currently includes:

```text
README.md
docs/PROJECT_SPECIFICATION.md
docs/api/
docs/diagrams/
docs/EVIDENCE_MATRIX.md
```

The final README documents:

- project purpose
- features
- technology stack
- architecture
- local setup
- Docker/PostgreSQL setup
- backend setup
- frontend setup
- database migrations
- API overview
- authentication
- RBAC
- testing
- security
- Git workflow
- demo sequence
- disclaimer
- license

Evidence:

- PR #10 — `docs: add final project README`

**Status:** Completed.

---

## 10. Final Demonstration Evidence

Recommended demonstration sequence:

```text
1. Start PostgreSQL with Docker
2. Start Spring Boot backend
3. Start React frontend
4. Login as administrator
5. Show dashboard statistics
6. Show user management
7. Show department management
8. Assign/revoke user role
9. Show role management
10. Grant/revoke role permission
11. Show system-role read-only protection
12. Show permission management
13. Deactivate/reactivate an account if required
14. Show audit log
15. Logout
16. Login again
17. Show recorded LOGOUT audit event
```

Functional evidence already verified during development includes:

```text
User CRUD / lifecycle
Department CRUD / hierarchy
Role lifecycle
Permission lifecycle
User-role assignment and revocation
Role-permission grant and revocation
System role protection
Dashboard live values
Audit log display
Logout -> HTTP 204
Logout SUCCESS audit record
```

---

## 11. Internship Task Coverage Summary

| Internship Area | Project Evidence | Status |
|---|---|---|
| Environment setup | Java, Node.js, Docker, PostgreSQL, Git | Completed |
| Frontend development | React CRUD/search/admin UI | Completed |
| Backend development | Java Spring Boot REST API | Completed |
| Database | PostgreSQL, relational schema, SQL, Flyway | Completed |
| SQL indexes | 12 explicit indexes in V1 migration | Completed |
| Git workflow | branches, commits and merged PRs | Completed |
| Authentication | login, JWT, refresh tokens, logout | Completed |
| Authorization | roles, permissions, RBAC | Completed |
| Security | BCrypt, validation, account lock, system protection | Completed |
| Audit | backend audit events and frontend viewer | Completed |
| Automated testing | 80 backend tests passing | Completed |
| Frontend verification | production Vite build passing | Completed |
| Documentation | README, specification, evidence matrix | Completed |

---

## 12. Final Technical Verification

Backend:

```text
Tests run: 80
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

Frontend:

```text
✓ 23 modules transformed
✓ production build completed successfully
```

Database:

```text
PostgreSQL 17
Flyway schema version: 4
4 migrations validated
```

Git:

```text
main synchronized with origin/main
feature development integrated through pull requests
```

---

## Conclusion

The Bank Access Management System demonstrates the main technical areas covered during the internship:

- full-stack development
- Java backend development
- React frontend development
- REST APIs
- relational database design
- SQL and indexing
- authentication
- authorization and RBAC
- application security
- audit logging
- automated testing
- Git workflow
- Docker-based infrastructure
- technical documentation

The project is ready to be used as practical evidence during internship review and final demonstration.