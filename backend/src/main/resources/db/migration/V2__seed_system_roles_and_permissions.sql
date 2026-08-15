INSERT INTO roles (
    id,
    code,
    name,
    description,
    system_role,
    active
)
VALUES
    (
        '10000000-0000-4000-8000-000000000001',
        'ADMIN',
        'Administrator',
        'Built-in administrator with full platform access',
        TRUE,
        TRUE
    ),
    (
        '10000000-0000-4000-8000-000000000002',
        'SECURITY_OFFICER',
        'Security Officer',
        'Built-in role for identity and access administration',
        TRUE,
        TRUE
    ),
    (
        '10000000-0000-4000-8000-000000000003',
        'AUDITOR',
        'Auditor',
        'Built-in read-only role for security and compliance reviews',
        TRUE,
        TRUE
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    system_role = TRUE,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP;


INSERT INTO permissions (
    id,
    code,
    name,
    description,
    active
)
VALUES
    (
        '20000000-0000-4000-8000-000000000001',
        'USER_VIEW',
        'View users',
        'View user accounts and user details',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000002',
        'USER_CREATE',
        'Create users',
        'Create new user accounts',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000003',
        'USER_UPDATE',
        'Update users',
        'Update existing user accounts',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000004',
        'USER_DEACTIVATE',
        'Deactivate users',
        'Deactivate user accounts',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000005',
        'DEPARTMENT_VIEW',
        'View departments',
        'View departments and department details',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000006',
        'DEPARTMENT_CREATE',
        'Create departments',
        'Create new departments',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000007',
        'DEPARTMENT_UPDATE',
        'Update departments',
        'Update existing departments',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000008',
        'DEPARTMENT_DEACTIVATE',
        'Deactivate departments',
        'Deactivate departments',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000009',
        'ROLE_VIEW',
        'View roles',
        'View roles and role assignments',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000010',
        'ROLE_CREATE',
        'Create roles',
        'Create new roles',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000011',
        'ROLE_UPDATE',
        'Update roles',
        'Update or activate existing roles',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000012',
        'ROLE_DEACTIVATE',
        'Deactivate roles',
        'Deactivate roles',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000013',
        'ROLE_ASSIGN',
        'Assign roles',
        'Assign roles to users',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000014',
        'ROLE_REVOKE',
        'Revoke roles',
        'Revoke roles from users',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000015',
        'PERMISSION_VIEW',
        'View permissions',
        'View permissions and role permission assignments',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000016',
        'PERMISSION_CREATE',
        'Create permissions',
        'Create new permissions',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000017',
        'PERMISSION_UPDATE',
        'Update permissions',
        'Update or activate existing permissions',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000018',
        'PERMISSION_DEACTIVATE',
        'Deactivate permissions',
        'Deactivate permissions',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000019',
        'PERMISSION_GRANT',
        'Grant permissions',
        'Grant permissions to roles',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000020',
        'PERMISSION_REVOKE',
        'Revoke permissions',
        'Revoke permissions from roles',
        TRUE
    ),
    (
        '20000000-0000-4000-8000-000000000021',
        'AUDIT_LOG_VIEW',
        'View audit logs',
        'View security and activity audit logs',
        TRUE
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP;


WITH role_permission_seed (
    role_code,
    permission_code
) AS (
    VALUES
        ('ADMIN', 'USER_VIEW'),
        ('ADMIN', 'USER_CREATE'),
        ('ADMIN', 'USER_UPDATE'),
        ('ADMIN', 'USER_DEACTIVATE'),
        ('ADMIN', 'DEPARTMENT_VIEW'),
        ('ADMIN', 'DEPARTMENT_CREATE'),
        ('ADMIN', 'DEPARTMENT_UPDATE'),
        ('ADMIN', 'DEPARTMENT_DEACTIVATE'),
        ('ADMIN', 'ROLE_VIEW'),
        ('ADMIN', 'ROLE_CREATE'),
        ('ADMIN', 'ROLE_UPDATE'),
        ('ADMIN', 'ROLE_DEACTIVATE'),
        ('ADMIN', 'ROLE_ASSIGN'),
        ('ADMIN', 'ROLE_REVOKE'),
        ('ADMIN', 'PERMISSION_VIEW'),
        ('ADMIN', 'PERMISSION_CREATE'),
        ('ADMIN', 'PERMISSION_UPDATE'),
        ('ADMIN', 'PERMISSION_DEACTIVATE'),
        ('ADMIN', 'PERMISSION_GRANT'),
        ('ADMIN', 'PERMISSION_REVOKE'),
        ('ADMIN', 'AUDIT_LOG_VIEW'),

        ('SECURITY_OFFICER', 'USER_VIEW'),
        ('SECURITY_OFFICER', 'USER_CREATE'),
        ('SECURITY_OFFICER', 'USER_UPDATE'),
        ('SECURITY_OFFICER', 'USER_DEACTIVATE'),
        ('SECURITY_OFFICER', 'DEPARTMENT_VIEW'),
        ('SECURITY_OFFICER', 'ROLE_VIEW'),
        ('SECURITY_OFFICER', 'ROLE_CREATE'),
        ('SECURITY_OFFICER', 'ROLE_UPDATE'),
        ('SECURITY_OFFICER', 'ROLE_DEACTIVATE'),
        ('SECURITY_OFFICER', 'ROLE_ASSIGN'),
        ('SECURITY_OFFICER', 'ROLE_REVOKE'),
        ('SECURITY_OFFICER', 'PERMISSION_VIEW'),
        ('SECURITY_OFFICER', 'PERMISSION_CREATE'),
        ('SECURITY_OFFICER', 'PERMISSION_UPDATE'),
        ('SECURITY_OFFICER', 'PERMISSION_DEACTIVATE'),
        ('SECURITY_OFFICER', 'PERMISSION_GRANT'),
        ('SECURITY_OFFICER', 'PERMISSION_REVOKE'),
        ('SECURITY_OFFICER', 'AUDIT_LOG_VIEW'),

        ('AUDITOR', 'USER_VIEW'),
        ('AUDITOR', 'DEPARTMENT_VIEW'),
        ('AUDITOR', 'ROLE_VIEW'),
        ('AUDITOR', 'PERMISSION_VIEW'),
        ('AUDITOR', 'AUDIT_LOG_VIEW')
)
INSERT INTO role_permissions (
    role_id,
    permission_id
)
SELECT
    role_record.id,
    permission_record.id
FROM role_permission_seed seed
JOIN roles role_record
    ON role_record.code = seed.role_code
JOIN permissions permission_record
    ON permission_record.code = seed.permission_code
ON CONFLICT (role_id, permission_id) DO NOTHING;