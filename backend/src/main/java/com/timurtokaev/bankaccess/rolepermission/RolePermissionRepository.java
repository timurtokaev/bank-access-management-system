package com.timurtokaev.bankaccess.rolepermission;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT rolePermission.permission.code
            FROM RolePermission rolePermission
            WHERE rolePermission.role.id = :roleId
            ORDER BY rolePermission.permission.code
            """)
    List<String> findPermissionCodesByRoleId(
            @Param("roleId") UUID roleId
    );
}
