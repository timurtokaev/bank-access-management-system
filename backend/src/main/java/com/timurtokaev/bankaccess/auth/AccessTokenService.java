package com.timurtokaev.bankaccess.auth;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class AccessTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final AuthTokenProperties tokenProperties;
    private final Clock clock;

    public AccessTokenService(
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties,
            AuthTokenProperties tokenProperties,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
        this.tokenProperties = tokenProperties;
        this.clock = clock;
    }

    public IssuedAccessToken issue(
            UUID userId,
            String username,
            Collection<String> permissionCodes
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID must not be null"
            );
        }

        String normalizedUsername =
                requireUsername(username);

        List<String> normalizedPermissions =
                normalizePermissions(permissionCodes);

        Instant issuedAt = clock.instant();

        Instant expiresAt = issuedAt.plus(
                tokenProperties.accessTokenTtl()
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .audience(
                        List.of(jwtProperties.audience())
                )
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .claim(
                        "username",
                        normalizedUsername
                )
                .claim(
                        "permissions",
                        normalizedPermissions
                )
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        Jwt jwt = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        header,
                        claims
                )
        );

        return new IssuedAccessToken(
                jwt.getTokenValue(),
                OffsetDateTime.ofInstant(
                        expiresAt,
                        ZoneOffset.UTC
                )
        );
    }

    private String requireUsername(
            String username
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be empty"
            );
        }

        return username.trim();
    }

    private List<String> normalizePermissions(
            Collection<String> permissionCodes
    ) {
        if (permissionCodes == null) {
            throw new IllegalArgumentException(
                    "Permission codes must not be null"
            );
        }

        return permissionCodes.stream()
                .map(this::requirePermissionCode)
                .distinct()
                .sorted()
                .toList();
    }

    private String requirePermissionCode(
            String permissionCode
    ) {
        if (permissionCode == null
                || permissionCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Permission code must not be empty"
            );
        }

        return permissionCode.trim();
    }
}