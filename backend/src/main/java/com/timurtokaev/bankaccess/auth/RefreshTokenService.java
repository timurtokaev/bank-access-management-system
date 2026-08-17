package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.common.error.UnauthorizedException;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserSessionRevoker;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class RefreshTokenService implements UserSessionRevoker {

    private static final int TOKEN_SIZE_BYTES = 32;

    private static final int TOKEN_ENCODED_LENGTH = 43;

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[A-Za-z0-9_-]{43}");
    private static final Base64.Encoder BASE64_URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthTokenProperties tokenProperties;
    private final Clock clock;
    private final SecureRandom secureRandom;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            AuthTokenProperties tokenProperties,
            Clock clock,
            SecureRandom secureRandom
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProperties = tokenProperties;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public IssuedRefreshToken issue(User user) {
        OffsetDateTime issuedAt = currentTime();

        return issue(user, issuedAt);
    }

    @Transactional
    public RotatedRefreshToken rotate(
            String rawToken
    ) {
        String tokenHash = hashRawToken(rawToken);

        UUID expectedUserId = refreshTokenRepository
                .findUserIdByTokenHash(tokenHash)
                .orElseThrow(UnauthorizedException::new);

        User user = userRepository
                .findByIdForUpdate(expectedUserId)
                .orElseThrow(UnauthorizedException::new);

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(UnauthorizedException::new);

        OffsetDateTime rotatedAt = currentTime();

        if (!Objects.equals(
                storedToken.getUser().getId(),
                expectedUserId
        )) {
            throw new UnauthorizedException();
        }

        if (!storedToken.isUsableAt(rotatedAt)
                || user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException();
        }

        storedToken.revoke(rotatedAt);

        refreshTokenRepository.saveAndFlush(
                storedToken
        );

        IssuedRefreshToken issuedToken = issue(
                user,
                rotatedAt
        );

        return new RotatedRefreshToken(
                user.getId(),
                issuedToken
        );
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hashRawToken(rawToken);

        Optional<RefreshToken> storedToken =
                refreshTokenRepository
                        .findByTokenHashForUpdate(tokenHash);

        if (storedToken.isEmpty()) {
            return;
        }

        RefreshToken refreshToken = storedToken.get();

        if (refreshToken.isRevoked()) {
            return;
        }

        refreshToken.revoke(currentTime());
        refreshTokenRepository.saveAndFlush(refreshToken);
    }

    @Transactional
    @Override
    public int revokeAllActiveForUser(UUID userId) {
        Objects.requireNonNull(
                userId,
                "User ID must not be null"
        );

        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot revoke sessions for missing user: "
                                + userId
                ));

        OffsetDateTime revokedAt = currentTime();

        return refreshTokenRepository
                .revokeAllActiveByUserId(
                        userId,
                        revokedAt
                );
    }

    private IssuedRefreshToken issue(
            User user,
            OffsetDateTime issuedAt
    ) {
        Objects.requireNonNull(
                user,
                "User must not be null"
        );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException();
        }

        String rawToken = generateRawToken();
        String tokenHash = hashRawToken(rawToken);

        OffsetDateTime expiresAt = issuedAt.plus(
                tokenProperties.refreshTokenTtl()
        );

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                issuedAt,
                expiresAt
        );

        refreshTokenRepository.saveAndFlush(refreshToken);

        return new IssuedRefreshToken(
                rawToken,
                expiresAt
        );
    }

    private String generateRawToken() {
        byte[] tokenBytes =
                new byte[TOKEN_SIZE_BYTES];

        secureRandom.nextBytes(tokenBytes);

        return BASE64_URL_ENCODER.encodeToString(
                tokenBytes
        );
    }

    private String hashRawToken(String rawToken) {
        String normalizedToken =
                normalizeRawToken(rawToken);

        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] tokenHash = messageDigest.digest(
                    normalizedToken.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return BASE64_URL_ENCODER.encodeToString(
                    tokenHash
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }

    private String normalizeRawToken(String rawToken) {
        if (rawToken == null
                || rawToken.length() != TOKEN_ENCODED_LENGTH
                || !TOKEN_PATTERN.matcher(rawToken).matches()) {
            throw new UnauthorizedException();
        }

        return rawToken;
    }

    private OffsetDateTime currentTime() {
        return OffsetDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );
    }
}
