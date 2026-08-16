package com.timurtokaev.bankaccess.auth;

import java.util.Objects;
import java.util.UUID;

public record RotatedRefreshToken(

        UUID userId,

        IssuedRefreshToken issuedToken

) {

    public RotatedRefreshToken {
        Objects.requireNonNull(
                userId,
                "User ID must not be null"
        );

        Objects.requireNonNull(
                issuedToken,
                "Issued refresh token must not be null"
        );
    }

    @Override
    public String toString() {
        return "RotatedRefreshToken["
                + "userId=" + userId
                + ", refreshToken=<redacted>"
                + ", expiresAt=" + issuedToken.expiresAt()
                + "]";
    }
}