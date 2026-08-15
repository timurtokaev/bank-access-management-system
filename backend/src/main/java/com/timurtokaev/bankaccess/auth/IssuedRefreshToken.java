package com.timurtokaev.bankaccess.auth;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record IssuedRefreshToken(

        String token,

        OffsetDateTime expiresAt

) {

    public IssuedRefreshToken {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token must not be empty"
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Refresh token expiration must not be null"
            );
        }

        expiresAt = expiresAt.withOffsetSameInstant(
                ZoneOffset.UTC
        );
    }

    @Override
    public String toString() {
        return "IssuedRefreshToken[expiresAt="
                + expiresAt
                + "]";
    }
}