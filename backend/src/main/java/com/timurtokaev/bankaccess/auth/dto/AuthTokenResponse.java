package com.timurtokaev.bankaccess.auth.dto;

import java.time.OffsetDateTime;

public record AuthTokenResponse(

        String tokenType,

        String accessToken,

        OffsetDateTime accessTokenExpiresAt,

        String refreshToken,

        OffsetDateTime refreshTokenExpiresAt

) {

    @Override
    public String toString() {
        return "AuthTokenResponse[" +
                "tokenType=" + tokenType +
                ", accessToken=<redacted>" +
                ", accessTokenExpiresAt=" + accessTokenExpiresAt +
                ", refreshToken=<redacted>" +
                ", refreshTokenExpiresAt=" + refreshTokenExpiresAt +
                "]";
    }
}