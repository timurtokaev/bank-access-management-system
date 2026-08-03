package com.timurtokaev.bankaccess.department.dto;

import java.util.UUID;

public record DepartmentCreateRequest(
        String code,
        String name,
        UUID parentId
) {
}