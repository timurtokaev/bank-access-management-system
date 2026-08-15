package com.timurtokaev.bankaccess.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.tokens")
public record AuthTokenProperties(

        Duration accessTokenTtl,

        Duration refreshTokenTtl

) {

    public AuthTokenProperties {
        accessTokenTtl = requirePositive(
                accessTokenTtl,
                "Access token TTL"
        );

        refreshTokenTtl = requirePositive(
                refreshTokenTtl,
                "Refresh token TTL"
        );
    }

    private static Duration requirePositive(
            Duration value,
            String fieldName
    ) {
        if (value == null
                || value.isZero()
                || value.isNegative()) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }

        return value;
    }
}