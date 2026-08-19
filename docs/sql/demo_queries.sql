-- Bank Access Management System
-- SQL demonstration queries
--
-- Purpose:
-- Evidence for the internship tasks related to PostgreSQL,
-- CRUD operations, relational queries and JOIN usage.
--
-- The CRUD demonstration is executed inside a transaction
-- and rolled back, so it does not modify the final project data.


-- ============================================================
-- 1. SELECT
-- Display active users
-- ============================================================

SELECT
    id,
    employee_number,
    username,
    email,
    first_name,
    last_name,
    status
FROM users
WHERE status = 'ACTIVE'
ORDER BY username;


-- ============================================================
-- 2. JOIN: User -> Department
-- Shows employees together with their organizational unit
-- ============================================================

SELECT
    u.employee_number,
    u.username,
    u.first_name,
    u.last_name,
    u.status,
    d.code AS department_code,
    d.name AS department_name
FROM users u
JOIN departments d
    ON d.id = u.department_id
ORDER BY u.username;


-- ============================================================
-- 3. JOIN: User -> Role
-- LEFT JOIN keeps users that currently have no assigned roles
-- ============================================================

SELECT
    u.username,
    r.code AS role_code,
    r.name AS role_name,
    ur.assigned_at,
    ur.expires_at,
    assigner.username AS assigned_by
FROM users u
LEFT JOIN user_roles ur
    ON ur.user_id = u.id
LEFT JOIN roles r
    ON r.id = ur.role_id
LEFT JOIN users assigner
    ON assigner.id = ur.assigned_by
ORDER BY u.username, r.code;


-- ============================================================
-- 4. JOIN: Role -> Permission
-- Demonstrates the core RBAC relationship
-- ============================================================

SELECT
    r.code AS role_code,
    r.name AS role_name,
    p.code AS permission_code,
    p.name AS permission_name,
    rp.granted_at
FROM roles r
LEFT JOIN role_permissions rp
    ON rp.role_id = r.id
LEFT JOIN permissions p
    ON p.id = rp.permission_id
ORDER BY r.code, p.code;


-- ============================================================
-- 5. JOIN: Audit Log -> User
-- Shows security events and the related actor account
-- ============================================================

SELECT
    al.occurred_at,
    al.action,
    al.entity_type,
    al.result,
    al.actor_username,
    u.employee_number,
    al.ip_address
FROM audit_logs al
LEFT JOIN users u
    ON u.id = al.actor_user_id
ORDER BY al.occurred_at DESC
LIMIT 50;


-- ============================================================
-- 6. Multi-table RBAC JOIN
-- User -> User Role -> Role -> Role Permission -> Permission
-- ============================================================

SELECT
    u.username,
    r.code AS role_code,
    p.code AS permission_code
FROM users u
JOIN user_roles ur
    ON ur.user_id = u.id
JOIN roles r
    ON r.id = ur.role_id
JOIN role_permissions rp
    ON rp.role_id = r.id
JOIN permissions p
    ON p.id = rp.permission_id
ORDER BY u.username, r.code, p.code;


-- ============================================================
-- 7. Safe CRUD demonstration
--
-- INSERT, SELECT, UPDATE and DELETE are executed inside
-- a transaction. ROLLBACK guarantees that the demonstration
-- leaves the database unchanged.
-- ============================================================

BEGIN;

-- INSERT
INSERT INTO departments (
    id,
    code,
    name,
    active
)
VALUES (
    '00000000-0000-0000-0000-00000000de01',
    'SQL_DEMO_TEMP',
    'Temporary SQL Demo Department',
    TRUE
);

-- SELECT
SELECT
    id,
    code,
    name,
    active
FROM departments
WHERE code = 'SQL_DEMO_TEMP';

-- UPDATE
UPDATE departments
SET
    name = 'Updated SQL Demo Department',
    active = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE code = 'SQL_DEMO_TEMP';

SELECT
    id,
    code,
    name,
    active
FROM departments
WHERE code = 'SQL_DEMO_TEMP';

-- DELETE
DELETE FROM departments
WHERE code = 'SQL_DEMO_TEMP';

-- Confirm that the temporary record was deleted
SELECT
    id,
    code,
    name,
    active
FROM departments
WHERE code = 'SQL_DEMO_TEMP';

-- Restore the database to its original state
ROLLBACK;