package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.role.Role;
import com.timurtokaev.bankaccess.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(
            name = "assigned_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime assignedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    protected UserRole() {
    }

    public UserRole(
            User user,
            Role role,
            User assignedBy,
            OffsetDateTime assignedAt,
            OffsetDateTime expiresAt
    ) {
        this.user = user;
        this.role = role;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        this.expiresAt = expiresAt;
        this.id = new UserRoleId(
                user.getId(),
                role.getId()
        );
    }

    @PrePersist
    private void beforeInsert() {
        if (id == null) {
            id = new UserRoleId(
                    user.getId(),
                    role.getId()
            );
        }

        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public UserRoleId getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Role getRole() {
        return role;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }
}