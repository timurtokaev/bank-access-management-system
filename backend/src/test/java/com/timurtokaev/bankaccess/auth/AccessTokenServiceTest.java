package com.timurtokaev.bankaccess.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccessTokenServiceTest {

    private static final String TEST_SECRET_BASE64 =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void shouldIssueAccessTokenWithExpectedClaims() {
        JwtProperties jwtProperties = new JwtProperties(
                TEST_SECRET_BASE64,
                "test-issuer",
                "test-audience"
        );

        AuthTokenProperties tokenProperties =
                new AuthTokenProperties(
                        Duration.ofMinutes(10),
                        Duration.ofDays(30)
                );

        Instant issuedAt = Instant.now()
                .truncatedTo(ChronoUnit.SECONDS);

        Clock clock = Clock.fixed(
                issuedAt,
                ZoneOffset.UTC
        );

        JwtConfig jwtConfig = new JwtConfig();

        SecretKey secretKey =
                jwtConfig.jwtSecretKey(jwtProperties);

        JwtEncoder encoder =
                jwtConfig.jwtEncoder(secretKey);

        JwtDecoder decoder =
                jwtConfig.jwtDecoder(
                        secretKey,
                        jwtProperties
                );

        AccessTokenService service =
                new AccessTokenService(
                        encoder,
                        jwtProperties,
                        tokenProperties,
                        clock
                );

        UUID userId = UUID.fromString(
                "00000000-0000-4000-8000-000000000001"
        );

        IssuedAccessToken issuedToken = service.issue(
                userId,
                " admin ",
                List.of(
                        "USER_VIEW",
                        "ROLE_VIEW",
                        "USER_VIEW"
                )
        );

        Jwt decodedToken = decoder.decode(
                issuedToken.token()
        );

        assertEquals(
                userId.toString(),
                decodedToken.getSubject()
        );

        assertEquals(
                "admin",
                decodedToken.getClaimAsString(
                        "username"
                )
        );

        assertEquals(
                List.of(
                        "ROLE_VIEW",
                        "USER_VIEW"
                ),
                decodedToken.getClaimAsStringList(
                        "permissions"
                )
        );

        assertEquals(
                jwtProperties.issuer(),
                decodedToken.getClaimAsString("iss")
        );

        assertEquals(
                List.of(jwtProperties.audience()),
                decodedToken.getAudience()
        );

        assertEquals(
                issuedAt,
                decodedToken.getIssuedAt()
        );

        assertEquals(
                issuedAt.plus(Duration.ofMinutes(10)),
                decodedToken.getExpiresAt()
        );

        assertEquals(
                OffsetDateTime.ofInstant(
                        issuedAt.plus(
                                Duration.ofMinutes(10)
                        ),
                        ZoneOffset.UTC
                ),
                issuedToken.expiresAt()
        );

        assertNotNull(
                decodedToken.getClaimAsString("jti")
        );

        assertFalse(
                issuedToken.toString().contains(
                        issuedToken.token()
                )
        );
    }
}