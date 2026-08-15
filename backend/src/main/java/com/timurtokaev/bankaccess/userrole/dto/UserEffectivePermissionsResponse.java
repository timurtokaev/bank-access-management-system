package com.timurtokaev.bankaccess.userrole.dto;

import com.timurtokaev.bankaccess.user.UserStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserEffectivePermissionsResponse(

        UUID userId,

        String employeeNumber,

        String username,

        UserStatus userStatus,

        OffsetDateTime evaluatedAt,

        List<String> permissionCodes

) {

    public UserEffectivePermissionsResponse {
        permissionCodes = List.copyOf(permissionCodes);
    }
}