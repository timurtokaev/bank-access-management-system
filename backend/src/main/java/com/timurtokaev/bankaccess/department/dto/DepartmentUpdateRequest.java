package com.timurtokaev.bankaccess.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DepartmentUpdateRequest(

        @NotBlank(message = "Department code must not be empty")
        @Size(
                max = 50,
                message = "Department code must not exceed 50 characters"
        )
        String code,

        @NotBlank(message = "Department name must not be empty")
        @Size(
                max = 150,
                message = "Department name must not exceed 150 characters"
        )
        String name,

        UUID parentId
) {
}