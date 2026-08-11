package com.timurtokaev.bankaccess.user.dto;

import com.timurtokaev.bankaccess.user.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(

        UUID id,

        String employeeNumber,

        String username,

        String email,

        String firstName,

        String lastName,

        UUID departmentId,

        String departmentCode,

        String departmentName,

        UserStatus status,

        int failedLoginAttempts,

        OffsetDateTime lockedUntil,

        OffsetDateTime lastLoginAt,

        OffsetDateTime createdAt,

        OffsetDateTime updatedAt
) {
}