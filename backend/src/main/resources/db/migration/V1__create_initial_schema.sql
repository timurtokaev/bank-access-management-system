CREATE TABLE departments
(
    id         UUID         NOT NULL,
    code       VARCHAR(50)  NOT NULL,
    name       VARCHAR(150) NOT NULL,
    parent_id  UUID,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_departments PRIMARY KEY (id),
    CONSTRAINT uk_departments_code UNIQUE (code),
    CONSTRAINT fk_departments_parent
        FOREIGN KEY (parent_id)
            REFERENCES departments (id)
            ON DELETE RESTRICT
);

CREATE TABLE users
(
    id                    UUID         NOT NULL,
    employee_number       VARCHAR(50)  NOT NULL,
    username              VARCHAR(100) NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    first_name            VARCHAR(100) NOT NULL,
    last_name             VARCHAR(100) NOT NULL,
    department_id         UUID         NOT NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    failed_login_attempts INTEGER      NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_employee_number UNIQUE (employee_number),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),

    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id)
            REFERENCES departments (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),

    CONSTRAINT chk_users_failed_login_attempts
        CHECK (failed_login_attempts >= 0)
);

CREATE TABLE roles
(
    id          UUID         NOT NULL,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    system_role BOOLEAN      NOT NULL DEFAULT FALSE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_code UNIQUE (code)
);

CREATE TABLE permissions
(
    id          UUID         NOT NULL,
    code        VARCHAR(100) NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_code UNIQUE (code)
);

CREATE TABLE user_roles
(
    user_id     UUID        NOT NULL,
    role_id     UUID        NOT NULL,
    assigned_by UUID,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMPTZ,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_assigned_by
        FOREIGN KEY (assigned_by)
            REFERENCES users (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_user_roles_expiration
        CHECK (expires_at IS NULL OR expires_at > assigned_at)
);

CREATE TABLE role_permissions
(
    role_id      UUID        NOT NULL,
    permission_id UUID       NOT NULL,
    granted_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_role_permissions
        PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
            REFERENCES permissions (id)
            ON DELETE CASCADE
);

CREATE TABLE refresh_tokens
(
    id         UUID         NOT NULL,
    user_id    UUID         NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT,

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_refresh_tokens_expiration
        CHECK (expires_at > created_at)
);

CREATE TABLE audit_logs
(
    id             UUID         NOT NULL,
    actor_user_id  UUID,
    actor_username VARCHAR(100),
    action         VARCHAR(100) NOT NULL,
    entity_type    VARCHAR(100),
    entity_id      UUID,
    result         VARCHAR(20)  NOT NULL,
    ip_address     INET,
    details        JSONB        NOT NULL DEFAULT '{}'::JSONB,
    occurred_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),

    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_user_id)
            REFERENCES users (id)
            ON DELETE SET NULL,

    CONSTRAINT chk_audit_logs_result
        CHECK (result IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_departments_parent_id
    ON departments (parent_id);

CREATE INDEX idx_users_department_id
    ON users (department_id);

CREATE INDEX idx_users_status
    ON users (status);

CREATE INDEX idx_user_roles_role_id
    ON user_roles (role_id);

CREATE INDEX idx_user_roles_assigned_by
    ON user_roles (assigned_by);

CREATE INDEX idx_role_permissions_permission_id
    ON role_permissions (permission_id);

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expires_at
    ON refresh_tokens (expires_at);

CREATE INDEX idx_audit_logs_actor_user_id
    ON audit_logs (actor_user_id);

CREATE INDEX idx_audit_logs_occurred_at
    ON audit_logs (occurred_at);

CREATE INDEX idx_audit_logs_entity_type_entity_id
    ON audit_logs (entity_type, entity_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs (action);