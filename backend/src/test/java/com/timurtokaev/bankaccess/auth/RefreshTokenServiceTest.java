package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.common.error.UnauthorizedException;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final UUID USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    private static final OffsetDateTime NOW =
            OffsetDateTime.parse(
                    "2026-08-16T12:00:00Z"
            );

    private static final byte OLD_TOKEN_BYTE = 1;
    private static final byte NEW_TOKEN_BYTE = 7;

    private static final String OLD_RAW_TOKEN =
            createRawToken(OLD_TOKEN_BYTE);

    private static final String OLD_TOKEN_HASH =
            hashToken(OLD_RAW_TOKEN);

    private static final String NEW_RAW_TOKEN =
            createRawToken(NEW_TOKEN_BYTE);

    private static final String NEW_TOKEN_HASH =
            hashToken(NEW_RAW_TOKEN);

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecureRandom secureRandom;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        AuthTokenProperties tokenProperties =
                new AuthTokenProperties(
                        Duration.ofMinutes(10),
                        Duration.ofDays(30)
                );

        Clock clock = Clock.fixed(
                NOW.toInstant(),
                ZoneOffset.UTC
        );

        refreshTokenService =
                new RefreshTokenService(
                        refreshTokenRepository,
                        userRepository,
                        tokenProperties,
                        clock,
                        secureRandom
                );
    }

    @Test
    void shouldRotateTokenUsingUserThenTokenLockOrder() {
        User user = createUser(UserStatus.ACTIVE);

        RefreshToken storedToken =
                createStoredToken(user);

        stubLockedLookup(user, storedToken);

        doAnswer(invocation -> {
            byte[] tokenBytes =
                    invocation.getArgument(0);

            Arrays.fill(
                    tokenBytes,
                    NEW_TOKEN_BYTE
            );

            return null;
        }).when(secureRandom)
                .nextBytes(any(byte[].class));

        RotatedRefreshToken result =
                refreshTokenService.rotate(
                        OLD_RAW_TOKEN
                );

        verifyLockOrder();

        assertEquals(USER_ID, result.userId());

        assertEquals(
                NEW_RAW_TOKEN,
                result.issuedToken().token()
        );

        assertEquals(
                NOW.plusDays(30),
                result.issuedToken().expiresAt()
        );

        assertEquals(
                NOW,
                storedToken.getRevokedAt()
        );

        ArgumentCaptor<RefreshToken> savedTokenCaptor =
                ArgumentCaptor.forClass(
                        RefreshToken.class
                );

        verify(
                refreshTokenRepository,
                times(2)
        ).saveAndFlush(
                savedTokenCaptor.capture()
        );

        List<RefreshToken> savedTokens =
                savedTokenCaptor.getAllValues();

        assertSame(
                storedToken,
                savedTokens.get(0)
        );

        RefreshToken newStoredToken =
                savedTokens.get(1);

        assertSame(
                user,
                newStoredToken.getUser()
        );

        assertEquals(
                NEW_TOKEN_HASH,
                newStoredToken.getTokenHash()
        );

        assertNotEquals(
                NEW_RAW_TOKEN,
                newStoredToken.getTokenHash()
        );

        assertEquals(
                NOW,
                newStoredToken.getCreatedAt()
        );

        assertEquals(
                NOW.plusDays(30),
                newStoredToken.getExpiresAt()
        );

        assertNull(
                newStoredToken.getRevokedAt()
        );

        verify(secureRandom)
                .nextBytes(any(byte[].class));
    }

    @Test
    void shouldRejectInactiveUserWithoutSavingNewToken() {
        User user = createUser(
                UserStatus.INACTIVE
        );

        RefreshToken storedToken =
                createStoredToken(user);

        stubLockedLookup(user, storedToken);

        assertThrows(
                UnauthorizedException.class,
                () -> refreshTokenService.rotate(
                        OLD_RAW_TOKEN
                )
        );

        verifyLockOrder();

        assertNull(storedToken.getRevokedAt());

        verify(
                refreshTokenRepository,
                never()
        ).saveAndFlush(
                any(RefreshToken.class)
        );

        verifyNoInteractions(secureRandom);
    }

    @Test
    void shouldRejectAlreadyRevokedTokenWithoutSavingNewToken() {
        User user = createUser(
                UserStatus.ACTIVE
        );

        RefreshToken storedToken =
                createStoredToken(user);

        storedToken.revoke(
                NOW.minusMinutes(1)
        );

        stubLockedLookup(user, storedToken);

        assertThrows(
                UnauthorizedException.class,
                () -> refreshTokenService.rotate(
                        OLD_RAW_TOKEN
                )
        );

        verifyLockOrder();

        assertEquals(
                NOW.minusMinutes(1),
                storedToken.getRevokedAt()
        );

        verify(
                refreshTokenRepository,
                never()
        ).saveAndFlush(
                any(RefreshToken.class)
        );

        verifyNoInteractions(secureRandom);
    }

    @Test
    void shouldRejectMalformedTokenBeforeRepositoryLookup() {
        assertThrows(
                UnauthorizedException.class,
                () -> refreshTokenService.rotate(
                        "invalid-token"
                )
        );

        verifyNoInteractions(
                refreshTokenRepository,
                userRepository,
                secureRandom
        );
    }

    private void stubLockedLookup(
            User user,
            RefreshToken storedToken
    ) {
        when(
                refreshTokenRepository
                        .findUserIdByTokenHash(
                                OLD_TOKEN_HASH
                        )
        ).thenReturn(
                Optional.of(USER_ID)
        );

        when(
                userRepository.findByIdForUpdate(
                        USER_ID
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                refreshTokenRepository
                        .findByTokenHashForUpdate(
                                OLD_TOKEN_HASH
                        )
        ).thenReturn(
                Optional.of(storedToken)
        );
    }

    private void verifyLockOrder() {
        InOrder lockOrder = inOrder(
                refreshTokenRepository,
                userRepository
        );

        lockOrder.verify(
                refreshTokenRepository
        ).findUserIdByTokenHash(
                OLD_TOKEN_HASH
        );

        lockOrder.verify(
                userRepository
        ).findByIdForUpdate(
                USER_ID
        );

        lockOrder.verify(
                refreshTokenRepository
        ).findByTokenHashForUpdate(
                OLD_TOKEN_HASH
        );
    }

    private User createUser(
            UserStatus status
    ) {
        User user = new User(
                "EMP_001",
                "admin",
                "admin@example.com",
                "stored-password-hash",
                "Local",
                "Administrator",
                null
        );

        ReflectionTestUtils.setField(
                user,
                "id",
                USER_ID
        );

        user.setStatus(status);

        return user;
    }

    private RefreshToken createStoredToken(
            User user
    ) {
        return new RefreshToken(
                user,
                OLD_TOKEN_HASH,
                NOW.minusHours(1),
                NOW.plusDays(1)
        );
    }

    private static String createRawToken(
            byte tokenByte
    ) {
        byte[] tokenBytes = new byte[32];

        Arrays.fill(
                tokenBytes,
                tokenByte
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private static String hashToken(
            String rawToken
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hashBytes = digest.digest(
                    rawToken.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new AssertionError(
                    "SHA-256 must be available",
                    exception
            );
        }
    }
}