package com.timurtokaev.bankaccess.rolepermission.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RolePermissionGrantRequest(

        @NotNull(message = "Permission ID must not be null")
        UUID permissionId
) {
}