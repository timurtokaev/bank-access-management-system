package com.timurtokaev.bankaccess.userrole.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRoleResponse(

        UUID userId,

        String employeeNumber,

        String username,

        UUID roleId,

        String roleCode,

        String roleName,

        UUID assignedById,

        String assignedByUsername,

        OffsetDateTime assignedAt,

        OffsetDateTime expiresAt

) {
}