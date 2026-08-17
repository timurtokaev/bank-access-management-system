package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.audit.AuditLogWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class TransactionalLogoutService {

    private static final Map<String, Object> LOGOUT_DETAILS =
            Map.of(
                    "authenticationMethod",
                    "REFRESH_TOKEN"
            );

    private final RefreshTokenService refreshTokenService;
    private final AuditLogWriter auditLogWriter;

    public TransactionalLogoutService(
            RefreshTokenService refreshTokenService,
            AuditLogWriter auditLogWriter
    ) {
        this.refreshTokenService =
                refreshTokenService;

        this.auditLogWriter =
                auditLogWriter;
    }

    @Transactional
    public void execute(
            String rawRefreshToken
    ) {
        refreshTokenService
                .revoke(rawRefreshToken)
                .ifPresent(this::recordLogout);
    }

    private void recordLogout(
            RevokedRefreshToken revokedToken
    ) {
        auditLogWriter.write(
                revokedToken.userId(),
                revokedToken.username(),
                "LOGOUT",
                "USER",
                revokedToken.userId(),
                "SUCCESS",
                LOGOUT_DETAILS
        );
    }
}