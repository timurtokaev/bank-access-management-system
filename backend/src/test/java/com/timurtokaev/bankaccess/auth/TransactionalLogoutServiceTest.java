package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.audit.AuditLogWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionalLogoutServiceTest {

    private static final UUID USER_ID =
            UUID.fromString(
                    "00000000-0000-4000-8000-000000000001"
            );

    private static final String USERNAME =
            "admin";

    private static final String RAW_REFRESH_TOKEN =
            "A".repeat(43);

    private static final Map<String, Object> LOGOUT_DETAILS =
            Map.of(
                    "authenticationMethod",
                    "REFRESH_TOKEN"
            );

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuditLogWriter auditLogWriter;

    private TransactionalLogoutService
            transactionalLogoutService;

    @BeforeEach
    void setUp() {
        transactionalLogoutService =
                new TransactionalLogoutService(
                        refreshTokenService,
                        auditLogWriter
                );
    }

    @Test
    void shouldAuditSuccessfulLogoutWhenTokenIsRevoked() {
        RevokedRefreshToken revokedToken =
                new RevokedRefreshToken(
                        USER_ID,
                        USERNAME
                );

        when(
                refreshTokenService.revoke(
                        RAW_REFRESH_TOKEN
                )
        ).thenReturn(
                Optional.of(revokedToken)
        );

        transactionalLogoutService.execute(
                RAW_REFRESH_TOKEN
        );

        verify(refreshTokenService)
                .revoke(RAW_REFRESH_TOKEN);

        verify(auditLogWriter).write(
                USER_ID,
                USERNAME,
                "LOGOUT",
                "USER",
                USER_ID,
                "SUCCESS",
                LOGOUT_DETAILS
        );
    }

    @Test
    void shouldNotAuditLogoutWhenNothingWasRevoked() {
        when(
                refreshTokenService.revoke(
                        RAW_REFRESH_TOKEN
                )
        ).thenReturn(
                Optional.empty()
        );

        transactionalLogoutService.execute(
                RAW_REFRESH_TOKEN
        );

        verify(refreshTokenService)
                .revoke(RAW_REFRESH_TOKEN);

        verifyNoInteractions(
                auditLogWriter
        );
    }
}