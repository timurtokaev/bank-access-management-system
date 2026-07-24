# Bank Identity & Access Management System (BIAMS)

## 1. Project Overview

Bank Identity & Access Management System (BIAMS) is a web application designed to manage employees, roles, permissions and access control inside a banking organization.

The system allows administrators to create and manage user accounts, assign roles, configure permissions, monitor user activity and maintain audit logs.

This project is developed as part of an internship at Bank CenterCredit (BCC) and demonstrates modern enterprise software architecture using Java Spring Boot, React and PostgreSQL.

---

## 2. Project Goals

The main goals of the project are:

- Learn enterprise application architecture.
- Practice backend development using Spring Boot.
- Practice frontend development using React.
- Design and implement relational databases.
- Implement secure authentication and authorization.
- Demonstrate Role-Based Access Control (RBAC).
- Build a project suitable for a professional portfolio.

---

## 3. Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- Maven

### Frontend

- React
- TypeScript
- Vite
- Material UI

### Database

- PostgreSQL

### Other Tools

- Docker
- Git
- GitHub
- Postman
- IntelliJ IDEA

---

# 4. Core Modules

The system consists of the following functional modules:

## Authentication

Responsible for user authentication and authorization.

Features:
- Login
- Logout
- JWT Authentication
- Password Encryption

---

## User Management

Responsible for managing employee accounts.

Features:
- Create User
- Edit User
- Delete User
- Activate / Deactivate User
- Search Users
- Filter Users

---

## Role Management

Responsible for creating and assigning roles.

Examples:

- ADMIN
- SECURITY_OFFICER
- MANAGER
- AUDITOR
- SUPPORT

---

## Permission Management

Responsible for assigning permissions to roles.

Examples:

- CREATE_USER
- EDIT_USER
- DELETE_USER
- VIEW_USERS
- MANAGE_ROLES
- VIEW_AUDIT_LOGS

---

## Department Management

Stores information about bank departments.

Examples:

- IT
- Information Security
- HR
- Retail Banking
- Corporate Banking

---

## Audit Log

Stores every important action performed inside the system.

Examples:

- Login
- Logout
- User Created
- User Updated
- User Deleted
- Role Assigned