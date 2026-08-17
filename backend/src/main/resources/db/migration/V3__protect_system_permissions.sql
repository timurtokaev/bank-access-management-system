ALTER TABLE permissions
    ADD COLUMN system_permission BOOLEAN NOT NULL DEFAULT FALSE;

DO $migration$
DECLARE
    system_codes CONSTANT TEXT[] := ARRAY[
        'USER_VIEW',
        'USER_CREATE',
        'USER_UPDATE',
        'USER_DEACTIVATE',

        'DEPARTMENT_VIEW',
        'DEPARTMENT_CREATE',
        'DEPARTMENT_UPDATE',
        'DEPARTMENT_DEACTIVATE',

        'ROLE_VIEW',
        'ROLE_CREATE',
        'ROLE_UPDATE',
        'ROLE_DEACTIVATE',
        'ROLE_ASSIGN',
        'ROLE_REVOKE',

        'PERMISSION_VIEW',
        'PERMISSION_CREATE',
        'PERMISSION_UPDATE',
        'PERMISSION_DEACTIVATE',
        'PERMISSION_GRANT',
        'PERMISSION_REVOKE',

        'AUDIT_LOG_VIEW'
    ];

    updated_count INTEGER;
BEGIN
    UPDATE permissions
    SET
        system_permission = TRUE,
        active = TRUE,
        updated_at = CURRENT_TIMESTAMP
    WHERE code = ANY(system_codes);

    GET DIAGNOSTICS updated_count = ROW_COUNT;

    IF updated_count <> cardinality(system_codes) THEN
        RAISE EXCEPTION
            'Expected % seeded system permissions, updated %',
            cardinality(system_codes),
            updated_count;
    END IF;
END
$migration$;

ALTER TABLE permissions
    ADD CONSTRAINT chk_permissions_system_active
        CHECK (NOT system_permission OR active);
