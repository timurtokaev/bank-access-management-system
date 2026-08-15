package com.timurtokaev.bankaccess.rolepermission.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RolePermissionResponse(

        UUID roleId,

        String roleCode,

        String roleName,

        UUID permissionId,

        String permissionCode,

        String permissionName,

        OffsetDateTime grantedAt
) {
}