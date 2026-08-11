package com.timurtokaev.bankaccess.permission.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PermissionResponse(

        UUID id,

        String code,

        String name,

        String description,

        boolean active,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}