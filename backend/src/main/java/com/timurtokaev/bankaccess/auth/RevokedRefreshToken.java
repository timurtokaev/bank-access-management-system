package com.timurtokaev.bankaccess.auth;

import java.util.Objects;
import java.util.UUID;

public record RevokedRefreshToken(

        UUID userId,

        String username

) {

    public RevokedRefreshToken {
        Objects.requireNonNull(
                userId,
                "User ID must not be null"
        );

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be empty"
            );
        }

        username = username.trim();
    }
}