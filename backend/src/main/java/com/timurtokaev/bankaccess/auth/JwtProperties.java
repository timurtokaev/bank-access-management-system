package com.timurtokaev.bankaccess.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.Base64;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(

        String secretBase64,

        String issuer,

        String audience

) {

    private static final int MIN_SECRET_SIZE_BYTES = 32;

    public JwtProperties {
        secretBase64 = requireValidSecret(secretBase64);
        issuer = requireText(issuer, "JWT issuer");
        audience = requireText(audience, "JWT audience");
    }

    private static String requireValidSecret(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT secret must not be empty"
            );
        }

        byte[] secretBytes;

        try {
            secretBytes = Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "JWT secret must be valid Base64",
                    exception
            );
        }

        try {
            if (secretBytes.length < MIN_SECRET_SIZE_BYTES) {
                throw new IllegalArgumentException(
                        "JWT secret must contain at least 32 bytes"
                );
            }
        } finally {
            Arrays.fill(secretBytes, (byte) 0);
        }

        return value;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be empty"
            );
        }

        return value.trim();
    }

    @Override
    public String toString() {
        return "JwtProperties[secretBase64=<redacted>"
                + ", issuer="
                + issuer
                + ", audience="
                + audience
                + "]";
    }
}