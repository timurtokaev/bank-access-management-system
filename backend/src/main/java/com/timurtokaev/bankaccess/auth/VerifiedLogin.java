package com.timurtokaev.bankaccess.auth;

import java.util.UUID;

public record VerifiedLogin(

        UUID userId,

        String username

) {

    public VerifiedLogin {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "Verified user ID must not be null"
            );
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Verified username must not be empty"
            );
        }

        username = username.trim();
    }
}