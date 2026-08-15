package com.timurtokaev.bankaccess.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(

        @NotBlank(message = "Role code must not be empty")
        @Size(
                max = 100,
                message = "Role code must not exceed 100 characters"
        )
        @Pattern(
                regexp = "^[A-Z][A-Z0-9_]*$",
                message = "Role code must contain only uppercase letters, digits and underscores"
        )
        String code,

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