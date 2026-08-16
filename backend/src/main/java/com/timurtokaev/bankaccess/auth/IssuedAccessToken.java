package com.timurtokaev.bankaccess.auth;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record IssuedAccessToken(

        String token,

        OffsetDateTime expiresAt

) {

    public IssuedAccessToken {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token must not be empty"
            );
        }

        if (expiresAt == null) {
            throw new IllegalArgumentException(
                    "Access token expiration must not be null"
            );
        }

        expiresAt = expiresAt.withOffsetSameInstant(
                ZoneOffset.UTC
        );
    }

    @Override
    public String toString() {
        return "IssuedAccessToken[expiresAt="
                + expiresAt
                + "]";
    }
}