package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 255
    )
    private String tokenHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    protected RefreshToken() {
    }

    public RefreshToken(
            User user,
            String tokenHash,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt
    ) {
        this.user = Objects.requireNonNull(
                user,
                "User must not be null"
        );

        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Token hash must not be empty"
            );
        }

        this.tokenHash = tokenHash.trim();

        this.createdAt = toUtc(
                createdAt,
                "Created time"
        );

        this.expiresAt = toUtc(
                expiresAt,
                "Expiration time"
        );

        if (!this.expiresAt.isAfter(this.createdAt)) {
            throw new IllegalArgumentException(
                    "Refresh token expiration must be after creation"
            );
        }
    }

    public void revoke(OffsetDateTime revokedAt) {
        if (this.revokedAt != null) {
            return;
        }

        OffsetDateTime normalizedRevokedAt = toUtc(
                revokedAt,
                "Revocation time"
        );

        if (normalizedRevokedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "Revocation time must not be before creation"
            );
        }

        this.revokedAt = normalizedRevokedAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpiredAt(OffsetDateTime time) {
        OffsetDateTime normalizedTime = toUtc(
                time,
                "Evaluation time"
        );

        return !expiresAt.isAfter(normalizedTime);
    }

    public boolean isUsableAt(OffsetDateTime time) {
        return !isRevoked() && !isExpiredAt(time);
    }

    private static OffsetDateTime toUtc(
            OffsetDateTime value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null"
            );
        }

        return value.withOffsetSameInstant(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}