package com.timurtokaev.bankaccess.role.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RoleResponse(

        UUID id,

        String code,

        String name,

        String description,

        boolean systemRole,

        boolean active,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}
