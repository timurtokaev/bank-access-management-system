package com.timurtokaev.bankaccess.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionUpdateRequest(

        @NotBlank(message = "Permission name must not be empty")
        @Size(
                max = 150,
                message = "Permission name must not exceed 150 characters"
        )
        String name,

        @Size(
                max = 1000,
                message = "Permission description must not exceed 1000 characters"
        )
        String description
) {
}