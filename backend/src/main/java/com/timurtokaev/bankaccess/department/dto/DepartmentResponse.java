package com.timurtokaev.bankaccess.department.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String code,
        String name,
        UUID parentId,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}