package com.timurtokaev.bankaccess.userrole.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRoleAssignRequest(

        @NotNull(message = "Role ID must not be null")
        UUID roleId,

        @Future(
                message = "Role expiration time must be in the future"
        )
        OffsetDateTime expiresAt

) {
}