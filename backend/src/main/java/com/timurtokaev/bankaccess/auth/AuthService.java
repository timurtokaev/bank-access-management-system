package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.auth.dto.AuthTokenResponse;
import com.timurtokaev.bankaccess.auth.dto.LoginRequest;
import com.timurtokaev.bankaccess.auth.dto.RefreshRequest;
import com.timurtokaev.bankaccess.common.error.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final TransactionalLoginService transactionalLoginService;
    private final TransactionalRefreshService transactionalRefreshService;

    public AuthService(
            TransactionalLoginService transactionalLoginService,
            TransactionalRefreshService transactionalRefreshService
    ) {
        this.transactionalLoginService =
                transactionalLoginService;

        this.transactionalRefreshService =
                transactionalRefreshService;
    }

    public AuthTokenResponse login(
            LoginRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Login request must not be null"
            );
        }

        return transactionalLoginService
                .execute(
                        request.username(),
                        request.password()
                )
                .orElseThrow(
                        UnauthorizedException::new
                );
    }

    public AuthTokenResponse refresh(
            RefreshRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Refresh request must not be null"
            );
        }

        return transactionalRefreshService.execute(
                request.refreshToken()
        );
    }
}