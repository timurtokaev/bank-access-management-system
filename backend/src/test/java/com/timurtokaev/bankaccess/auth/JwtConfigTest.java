package com.timurtokaev.bankaccess.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtConfigTest {

    private static final String TEST_SECRET_BASE64 =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void shouldEncodeAndDecodeJwtWithExpectedClaims() {
        JwtProperties properties = new JwtProperties(
                TEST_SECRET_BASE64,
                "test-issuer",
                "test-audience"
        );

        JwtConfig config = new JwtConfig();

        SecretKey secretKey =
                config.jwtSecretKey(properties);

        JwtEncoder encoder =
                config.jwtEncoder(secretKey);

        JwtDecoder decoder =
                config.jwtDecoder(secretKey, properties);

        Instant issuedAt = Instant.now();

        String subject =
                "00000000-0000-4000-8000-000000000001";

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .audience(List.of(properties.audience()))
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(300))
                .subject(subject)
                .claim(
                        "permissions",
                        List.of("USER_VIEW")
                )
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        Jwt encoded = encoder.encode(
                JwtEncoderParameters.from(
                        header,
                        claims
                )
        );

        Jwt decoded =
                decoder.decode(encoded.getTokenValue());

        assertEquals(
                subject,
                decoded.getSubject()
        );

        assertEquals(
                properties.issuer(),
                decoded.getClaimAsString("iss")
        );

        assertEquals(
                List.of(properties.audience()),
                decoded.getAudience()
        );

        assertEquals(
                List.of("USER_VIEW"),
                decoded.getClaimAsStringList(
                        "permissions"
                )
        );
    }
}