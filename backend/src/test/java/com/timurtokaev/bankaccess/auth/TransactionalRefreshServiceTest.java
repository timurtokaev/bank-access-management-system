package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.auth.dto.AuthTokenResponse;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import com.timurtokaev.bankaccess.userrole.EffectivePermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionalRefreshServiceTest {

    private static final UUID USER_ID =
            UUID.fromString(
                    "00000000-0000-4000-8000-000000000001"
            );

    private static final String RAW_REFRESH_TOKEN =
            "A".repeat(43);

    private static final OffsetDateTime NOW =
            OffsetDateTime.parse(
                    "2026-08-16T12:00:00Z"
            );

    private static final OffsetDateTime ACCESS_EXPIRES_AT =
            OffsetDateTime.parse(
                    "2026-08-16T12:10:00Z"
            );

    private static final OffsetDateTime REFRESH_EXPIRES_AT =
            OffsetDateTime.parse(
                    "2026-09-15T12:00:00Z"
            );

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EffectivePermissionService effectivePermissionService;

    @Mock
    private AccessTokenService accessTokenService;

    private TransactionalRefreshService transactionalRefreshService;

    @BeforeEach
    void setUp() {
        transactionalRefreshService =
                new TransactionalRefreshService(
                        refreshTokenService,
                        userRepository,
                        effectivePermissionService,
                        accessTokenService
                );
    }

    @Test
    void shouldRotateRefreshTokenAndIssueNewAccessToken() {
        User user = createUser(
                UserStatus.ACTIVE
        );

        IssuedRefreshToken issuedRefreshToken =
                new IssuedRefreshToken(
                        "new-refresh-token",
                        REFRESH_EXPIRES_AT
                );

        RotatedRefreshToken rotatedRefreshToken =
                new RotatedRefreshToken(
                        USER_ID,
                        issuedRefreshToken
                );

        EffectivePermissionService.Result permissions =
                new EffectivePermissionService.Result(
                        NOW,
                        List.of(
                                "ROLE_VIEW",
                                "USER_VIEW"
                        )
                );

        IssuedAccessToken issuedAccessToken =
                new IssuedAccessToken(
                        "new-access-token",
                        ACCESS_EXPIRES_AT
                );

        when(
                refreshTokenService.rotate(
                        RAW_REFRESH_TOKEN
                )
        ).thenReturn(rotatedRefreshToken);

        when(
                userRepository.findById(
                        USER_ID
                )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                effectivePermissionService.resolveFor(
                        user
                )
        ).thenReturn(permissions);

        when(
                accessTokenService.issue(
                        USER_ID,
                        "admin",
                        permissions.permissionCodes(),
                        user.getAuthVersion()
                )
        ).thenReturn(issuedAccessToken);

        AuthTokenResponse response =
                transactionalRefreshService.execute(
                        RAW_REFRESH_TOKEN
                );

        assertEquals(
                "Bearer",
                response.tokenType()
        );

        assertEquals(
                "new-access-token",
                response.accessToken()
        );

        assertEquals(
                ACCESS_EXPIRES_AT,
                response.accessTokenExpiresAt()
        );

        assertEquals(
                "new-refresh-token",
                response.refreshToken()
        );

        assertEquals(
                REFRESH_EXPIRES_AT,
                response.refreshTokenExpiresAt()
        );

        verify(refreshTokenService)
                .rotate(RAW_REFRESH_TOKEN);

        verify(userRepository)
                .findById(USER_ID);

        verify(effectivePermissionService)
                .resolveFor(user);

        verify(accessTokenService)
                .issue(
                        USER_ID,
                        "admin",
                        permissions.permissionCodes(),
                        user.getAuthVersion()
                );
    }

    @Test
    void shouldRejectRefreshWhenRotatedTokenUserWasNotFound() {
        IssuedRefreshToken issuedRefreshToken =
                new IssuedRefreshToken(
                        "new-refresh-token",
                        REFRESH_EXPIRES_AT
                );

        RotatedRefreshToken rotatedRefreshToken =
                new RotatedRefreshToken(
                        USER_ID,
                        issuedRefreshToken
                );

        when(
                refreshTokenService.rotate(
                        RAW_REFRESH_TOKEN
                )
        ).thenReturn(rotatedRefreshToken);

        when(
                userRepository.findById(
                        USER_ID
                )
        ).thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> transactionalRefreshService
                                .execute(
                                        RAW_REFRESH_TOKEN
                                )
                );

        assertEquals(
                "Rotated refresh token user was not found",
                exception.getMessage()
        );

        verifyNoInteractions(
                effectivePermissionService,
                accessTokenService
        );
    }

    @Test
    void shouldRejectRefreshWhenUserIsNotActive() {
        User user = createUser(
                UserStatus.INACTIVE
        );

        IssuedRefreshToken issuedRefreshToken =
                new IssuedRefreshToken(
                        "new-refresh-token",
                        REFRESH_EXPIRES_AT
                );

        RotatedRefreshToken rotatedRefreshToken =
                new RotatedRefreshToken(
                        USER_ID,
                        issuedRefreshToken
                );

        when(
                refreshTokenService.rotate(
                        RAW_REFRESH_TOKEN
                )
        ).thenReturn(rotatedRefreshToken);

        when(
                userRepository.findById(
                        USER_ID
                )
        ).thenReturn(
                Optional.of(user)
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> transactionalRefreshService
                                .execute(
                                        RAW_REFRESH_TOKEN
                                )
                );

        assertEquals(
                "Rotated refresh token user is not active",
                exception.getMessage()
        );

        verifyNoInteractions(
                effectivePermissionService,
                accessTokenService
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

        user.changeStatus(status);

        return user;
    }
}
