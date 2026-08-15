package com.timurtokaev.bankaccess.rolepermission;

import com.timurtokaev.bankaccess.permission.Permission;
import com.timurtokaev.bankaccess.role.Role;
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
@Table(name = "role_permissions")
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;

    @MapsId("roleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @MapsId("permissionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(
            name = "granted_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime grantedAt;

    protected RolePermission() {
    }

    public RolePermission(
            Role role,
            Permission permission
    ) {
        this.role = role;
        this.permission = permission;
        this.id = new RolePermissionId(
                role.getId(),
                permission.getId()
        );
    }

    @PrePersist
    private void beforeInsert() {
        if (id == null) {
            id = new RolePermissionId(
                    role.getId(),
                    permission.getId()
            );
        }

        if (grantedAt == null) {
            grantedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public RolePermissionId getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public Permission getPermission() {
        return permission;
    }

    public OffsetDateTime getGrantedAt() {
        return grantedAt;
    }
}