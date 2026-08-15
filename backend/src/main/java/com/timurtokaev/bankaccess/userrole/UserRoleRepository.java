package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.user.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRoleId> {

    @EntityGraph(
            attributePaths = {
                    "user",
                    "role",
                    "assignedBy"
            }
    )
    List<UserRole> findAllByUser_IdOrderByRole_CodeAsc(
            UUID userId
    );

    @EntityGraph(
            attributePaths = {
                    "user",
                    "role",
                    "assignedBy"
            }
    )
    List<UserRole>
    findAllByRole_IdOrderByUser_LastNameAscUser_FirstNameAsc(
            UUID roleId
    );

    @Query("""
            SELECT DISTINCT rolePermission.permission.code
            FROM UserRole userRole,
                 RolePermission rolePermission
            WHERE userRole.user.id = :userId
              AND userRole.user.status = :activeStatus
              AND userRole.role.id = rolePermission.role.id
              AND userRole.role.active = true
              AND rolePermission.permission.active = true
              AND (
                    userRole.expiresAt IS NULL
                    OR userRole.expiresAt > :now
              )
            ORDER BY rolePermission.permission.code
            """)
    List<String> findEffectivePermissionCodes(
            @Param("userId") UUID userId,
            @Param("activeStatus") UserStatus activeStatus,
            @Param("now") OffsetDateTime now
    );
}