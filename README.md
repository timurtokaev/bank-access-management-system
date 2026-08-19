# Bank Access Management System

A full-stack web application for managing employees, departments, roles, permissions, authentication and access control in a banking-style environment.

The project demonstrates a practical implementation of Role-Based Access Control (RBAC), JWT authentication, audit logging, relational database design and a modern Java/React architecture.

> This is an educational internship and portfolio project. It is not an official banking production system.

## Features

### Authentication and Security

- Login with username and password
- JWT access tokens
- Database-backed refresh tokens
- Logout with refresh-token revocation
- Password hashing with BCrypt
- Failed-login protection and temporary account lockout
- Spring Security authorization
- Backend request validation
- Role-Based Access Control (RBAC)

### User Management

- View employees
- Search users
- Create users
- Edit users
- Assign departments
- Activate and deactivate accounts
- Assign and revoke roles
- View user roles

### Department Management

- View departments
- Search departments
- Create departments
- Edit departments
- Configure parent departments
- Deactivate departments
- Protection against invalid hierarchy cycles

### Role Management

- View roles
- Search roles
- Create custom roles
- Edit roles
- Activate and deactivate roles
- Protected system roles
- Assign roles to users
- Revoke roles from users

### Permission Management

- View permissions
- Search permissions
- Create custom permissions
- Edit permissions
- Activate and deactivate permissions
- Protected system permissions

### Role Permissions

- View permissions assigned to a role
- Grant permissions to custom active roles
- Revoke permissions from custom roles
- Read-only permissions view for system roles
- Backend delegation policy checks

### Audit Log

- View recent audit events
- Search audit records
- Inspect event details
- Track authentication events such as logout
- Store actor, entity, result, timestamp and event metadata

### Dashboard

The administration dashboard displays live statistics for:

- Users
- Roles
- Permissions
- Audit events

It also provides quick navigation to the main administration modules.

## Technology Stack

### Backend

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Spring Security
- OAuth2 Resource Server
- Jakarta Validation
- Flyway
- Maven
- JUnit
- Mockito
- MockMvc

### Frontend

- React 19
- TypeScript
- Vite
- CSS
- Fetch API

### Database

- PostgreSQL 17
- Flyway database migrations

### Infrastructure and Tools

- Docker
- Docker Compose
- Git
- GitHub
- Maven Wrapper
- npm

## Architecture

```text
bank-access-management-system/
├── backend/
│   ├── src/main/java/
│   ├── src/main/resources/
│   │   └── db/migration/
│   ├── src/test/java/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── docker/
│   ├── .env.example
│   └── compose.yml
├── docs/
│   ├── api/
│   ├── diagrams/
│   └── PROJECT_SPECIFICATION.md
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── LICENSE
└── README.md
```

The frontend communicates with the backend through `/api`.

During local development, Vite proxies `/api` requests to:

```text
http://localhost:8080
```

## Main Data Model

The application uses the following core entities:

- Users
- Departments
- Roles
- Permissions
- User Roles
- Role Permissions
- Refresh Tokens
- Audit Logs

Relationships between users, roles and permissions implement the RBAC authorization model.

## Local Development

### Requirements

Install the following tools:

- Java 21
- Node.js 24 or compatible recent Node.js version
- npm
- Docker Desktop
- Git

## 1. Clone the Repository

```powershell
git clone https://github.com/timurtokaev/bank-access-management-system.git
cd bank-access-management-system
```

## 2. Configure PostgreSQL

Create the local Docker environment file:

```powershell
Copy-Item docker\.env.example docker\.env
```

Open:

```text
docker/.env
```

Example configuration:

```env
DB_NAME=bank_access_management
DB_USERNAME=bank_access_app
DB_PASSWORD=replace_with_local_password
DB_PORT=5433
```

Use your own local password.

## 3. Start PostgreSQL

From the project root:

```powershell
docker compose --env-file docker/.env -f docker/compose.yml up -d
```

Check the container:

```powershell
docker ps
```

The PostgreSQL container should be available on:

```text
localhost:5433
```

The Docker container name is:

```text
bank-access-postgres
```

## 4. Configure the Backend Local Profile

Local configuration files are intentionally excluded from Git because they may contain passwords and secrets.

Create:

```text
backend/src/main/resources/application-local.yaml
```

Example:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5433/bank_access_management
    username: bank_access_app
    password: replace_with_local_password

app:
  security:
    jwt:
      secret-base64: "replace_with_your_base64_encoded_secret"
```

The database password must match `docker/.env`.

Use a sufficiently strong Base64-encoded JWT secret for local development.

## 5. Start the Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The backend starts on:

```text
http://localhost:8080
```

Keep this terminal running.

## 6. Start the Frontend

Open another terminal:

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

The frontend is available at:

```text
http://localhost:5173
```

## Database Migrations

Flyway automatically validates and applies database migrations when the backend starts.

Migration files are stored in:

```text
backend/src/main/resources/db/migration/
```

The application uses Flyway instead of automatic Hibernate schema creation.

Hibernate validates the database structure against the application entities.

## API Overview

Main API groups include:

```text
/api/auth
/api/users
/api/departments
/api/roles
/api/permissions
/api/audit-logs
```

RBAC relationship endpoints include operations such as:

```text
/api/users/{userId}/roles
/api/users/{userId}/effective-permissions
/api/roles/{roleId}/permissions
/api/permissions/{permissionId}/roles
```

Most protected endpoints require a JWT access token:

```http
Authorization: Bearer <access-token>
```

## Authentication Flow

The authentication flow uses two token types:

1. The user logs in with username and password.
2. The backend returns an access token and refresh token.
3. The access token is used for protected API requests.
4. The refresh token represents the longer-lived session.
5. Logout revokes the refresh token.
6. Successful logout is written to the audit log.

The default access-token lifetime is short-lived, while refresh tokens are used for longer sessions.

## RBAC Model

Authorization is based on:

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

A user may have multiple roles.

A role may have multiple permissions.

The backend calculates effective permissions from assigned roles and enforces them using Spring Security.

System roles and system permissions are protected from unsafe modification through the administration interface and backend rules.

## Testing

### Backend

Make sure PostgreSQL is running and `application-local.yaml` is configured.

Then run:

```powershell
cd backend
.\mvnw.cmd test
```

Current verified result:

```text
Tests run: 80
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

The backend test suite includes unit, controller, security and integration tests.

### Frontend Production Build

```powershell
cd frontend
npm.cmd install
npm.cmd run build
```

Current verified build:

```text
23 modules transformed
BUILD SUCCESS
```

The build command performs TypeScript compilation and creates the Vite production bundle.

## Security Highlights

The project demonstrates several security practices:

- Password hashing instead of storing plaintext passwords
- JWT-based API authentication
- Refresh-token revocation
- Short-lived access tokens
- RBAC authorization
- Permission-based endpoint protection
- Protected system roles and permissions
- Input validation
- Failed-login account protection
- Audit logging
- Server-side authorization checks

Frontend restrictions are treated as usability controls only. Security-sensitive operations are also validated by the backend.

## Git Workflow

Development is organized through feature and fix branches.

Examples:

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
```

Changes are committed separately and integrated through pull requests into `main`.

This workflow provides a traceable development history and demonstrates practical Git/GitHub usage.

## Demo Flow

A typical project demonstration can follow this sequence:

1. Start PostgreSQL with Docker.
2. Start the Spring Boot backend.
3. Start the React frontend.
4. Log in as an administrator.
5. Show live dashboard statistics.
6. Open user management.
7. Create or edit an employee.
8. Assign a department.
9. Assign and revoke a role.
10. Open role management.
11. Manage permissions for a custom role.
12. Show that system-role permissions are read-only.
13. Open permission management.
14. Show audit records.
15. Log out.
16. Log back in and show the recorded logout audit event.

## Project Purpose

This project was created to practice and demonstrate:

- Full-stack application development
- Java enterprise backend development
- React and TypeScript frontend development
- REST API design
- PostgreSQL relational database design
- Database migrations
- Authentication and authorization
- RBAC
- Secure application development
- Automated testing
- Git branching and pull-request workflow
- Technical documentation

## Disclaimer

Bank Access Management System is an educational project created for internship practice and portfolio demonstration.

It is not an official product of Bank CenterCredit or any other financial institution and must not be treated as a production banking system.

## License

This project is licensed under the MIT License.

See [LICENSE](LICENSE) for details.