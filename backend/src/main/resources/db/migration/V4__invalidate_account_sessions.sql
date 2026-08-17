ALTER TABLE users
    ADD COLUMN auth_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE users
    ADD CONSTRAINT chk_users_auth_version
        CHECK (auth_version >= 0);

UPDATE refresh_tokens
SET revoked_at = CURRENT_TIMESTAMP
WHERE revoked_at IS NULL
  AND expires_at > CURRENT_TIMESTAMP;
