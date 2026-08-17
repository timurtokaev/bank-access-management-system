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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionalLoginServiceTest {

    private static final UUID USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    private static final OffsetDateTime ACCESS_EXPIRES_AT =
            OffsetDateTime.parse(
                    "2026-08-16T12:10:00Z"
            );

    private static final OffsetDateTime REFRESH_EXPIRES_AT =
            OffsetDateTime.parse(
                    "2026-09-15T12:00:00Z"
            );

    private static final long AUTH_VERSION = 7L;

    @Mock
    private LoginStateService loginStateService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EffectivePermissionService effectivePermissionService;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private User user;

    private TransactionalLoginService transactionalLoginService;

    @BeforeEach
    void setUp() {
        transactionalLoginService =
                new TransactionalLoginService(
                        loginStateService,
                        userRepository,
                        effectivePermissionService,
                        accessTokenService,
                        refreshTokenService
                );
    }

    @Test
    void shouldIssueTokenResponseForVerifiedActiveUser() {
        VerifiedLogin verifiedLogin =
                new VerifiedLogin(
                        USER_ID,
                        "admin"
                );

        List<String> permissionCodes = List.of(
                "ROLE_VIEW",
                "USER_VIEW"
        );

        EffectivePermissionService.Result permissions =
                new EffectivePermissionService.Result(
                        OffsetDateTime.parse(
                                "2026-08-16T12:00:00Z"
                        ),
                        permissionCodes
                );

        IssuedAccessToken accessToken =
                new IssuedAccessToken(
                        "access-token",
                        ACCESS_EXPIRES_AT
                );

        IssuedRefreshToken refreshToken =
                new IssuedRefreshToken(
                        "refresh-token",
                        REFRESH_EXPIRES_AT
                );

        when(
                loginStateService.verify(
                        "admin",
                        "correct-password"
                )
        ).thenReturn(Optional.of(verifiedLogin));

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));

        when(user.getStatus())
                .thenReturn(UserStatus.ACTIVE);

        when(user.getId())
                .thenReturn(USER_ID);

        when(user.getUsername())
                .thenReturn("admin");

        when(user.getAuthVersion())
                .thenReturn(AUTH_VERSION);

        when(effectivePermissionService.resolveFor(user))
                .thenReturn(permissions);

        when(
                accessTokenService.issue(
                        USER_ID,
                        "admin",
                        permissionCodes,
                        AUTH_VERSION
                )
        ).thenReturn(accessToken);

        when(refreshTokenService.issue(user))
                .thenReturn(refreshToken);

        Optional<AuthTokenResponse> result =
                transactionalLoginService.execute(
                        "admin",
                        "correct-password"
                );

        assertTrue(result.isPresent());

        AuthTokenResponse response =
                result.orElseThrow();

        assertEquals("Bearer", response.tokenType());
        assertEquals(
                "access-token",
                response.accessToken()
        );
        assertEquals(
                ACCESS_EXPIRES_AT,
                response.accessTokenExpiresAt()
        );
        assertEquals(
                "refresh-token",
                response.refreshToken()
        );
        assertEquals(
                REFRESH_EXPIRES_AT,
                response.refreshTokenExpiresAt()
        );

        verify(loginStateService).verify(
                "admin",
                "correct-password"
        );
        verify(userRepository).findById(USER_ID);
        verify(effectivePermissionService)
                .resolveFor(user);
        verify(accessTokenService).issue(
                USER_ID,
                "admin",
                permissionCodes,
                AUTH_VERSION
        );
        verify(refreshTokenService).issue(user);
    }

    @Test
    void shouldReturnEmptyWithoutIssuingTokensWhenVerificationFails() {
        when(
                loginStateService.verify(
                        "missing",
                        "wrong-password"
                )
        ).thenReturn(Optional.empty());

        Optional<AuthTokenResponse> result =
                transactionalLoginService.execute(
                        "missing",
                        "wrong-password"
                );

        assertTrue(result.isEmpty());

        verify(loginStateService).verify(
                "missing",
                "wrong-password"
        );

        verifyNoInteractions(
                userRepository,
                effectivePermissionService,
                accessTokenService,
                refreshTokenService
        );
    }
}
