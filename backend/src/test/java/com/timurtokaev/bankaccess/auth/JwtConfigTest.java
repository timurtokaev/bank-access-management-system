package com.timurtokaev.bankaccess.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

        UserAccessTokenValidator userValidator =
                mock(UserAccessTokenValidator.class);

        when(userValidator.validate(any(Jwt.class)))
                .thenReturn(
                        OAuth2TokenValidatorResult.success()
                );

        JwtDecoder decoder =
                config.jwtDecoder(
                        secretKey,
                        properties,
                        userValidator
                );

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
                .claim("auth_version", 0L)
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

        verify(userValidator).validate(any(Jwt.class));
    }

    @Test
    void shouldConvertPermissionClaimsToAuthoritiesWithoutPrefix() {
        JwtConfig config = new JwtConfig();

        JwtAuthenticationConverter converter =
                config.jwtAuthenticationConverter();

        Jwt jwt = Jwt.withTokenValue(
                        "test-token"
                )
                .header(
                        "alg",
                        "HS256"
                )
                .claim(
                        "sub",
                        "00000000-0000-4000-8000-000000000001"
                )
                .claim(
                        "permissions",
                        List.of(
                                "USER_VIEW",
                                "ROLE_VIEW"
                        )
                )
                .build();

        AbstractAuthenticationToken authentication =
                converter.convert(jwt);

        assertNotNull(authentication);

        List<String> authorities =
                authentication.getAuthorities()
                        .stream()
                        .map(
                                GrantedAuthority::getAuthority
                        )
                        .sorted()
                        .toList();

        assertEquals(
                List.of(
                        "FACTOR_BEARER",
                        "ROLE_VIEW",
                        "USER_VIEW"
                ),
                authorities
        );
    }

    @Test
    void shouldNotQueryAccountStateWhenStandardJwtValidationFails() {
        JwtProperties properties = new JwtProperties(
                TEST_SECRET_BASE64,
                "test-issuer",
                "test-audience"
        );

        JwtConfig config = new JwtConfig();
        SecretKey secretKey = config.jwtSecretKey(properties);
        JwtEncoder encoder = config.jwtEncoder(secretKey);

        UserAccessTokenValidator userValidator =
                mock(UserAccessTokenValidator.class);

        JwtDecoder decoder = config.jwtDecoder(
                secretKey,
                properties,
                userValidator
        );

        Instant issuedAt = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("wrong-issuer")
                .audience(List.of(properties.audience()))
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(300))
                .subject(
                        "00000000-0000-4000-8000-000000000001"
                )
                .claim("auth_version", 0L)
                .build();

        Jwt encoded = encoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256)
                                .build(),
                        claims
                )
        );

        assertThrows(
                JwtValidationException.class,
                () -> decoder.decode(
                        encoded.getTokenValue()
                )
        );

        verifyNoInteractions(userValidator);
    }
}
