package com.timurtokaev.bankaccess.rolepermission;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository
        extends JpaRepository<
        RolePermission,
        RolePermissionId
        > {

    @EntityGraph(attributePaths = {
            "role",
            "permission"
    })
    List<RolePermission>
    findAllByRole_IdOrderByPermission_CodeAsc(
            UUID roleId
    );

    @EntityGraph(attributePaths = {
            "role",
            "permission"
    })
    List<RolePermission>
    findAllByPermission_IdOrderByRole_CodeAsc(
            UUID permissionId
    );
}