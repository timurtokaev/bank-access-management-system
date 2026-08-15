package com.timurtokaev.bankaccess.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(

        @NotBlank(message = "Role name must not be empty")
        @Size(
                max = 150,
                message = "Role name must not exceed 150 characters"
        )
        String name,

        @Size(
                max = 1000,
                message = "Role description must not exceed 1000 characters"
        )
        String description
) {
}