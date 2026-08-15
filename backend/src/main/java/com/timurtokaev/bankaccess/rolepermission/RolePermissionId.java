package com.timurtokaev.bankaccess.rolepermission;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class RolePermissionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(
            name = "role_id",
            nullable = false
    )
    private UUID roleId;

    @Column(
            name = "permission_id",
            nullable = false
    )
    private UUID permissionId;

    protected RolePermissionId() {
    }

    public RolePermissionId(
            UUID roleId,
            UUID permissionId
    ) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof RolePermissionId that)) {
            return false;
        }

        return Objects.equals(roleId, that.roleId)
                && Objects.equals(
                permissionId,
                that.permissionId
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, permissionId);
    }
}