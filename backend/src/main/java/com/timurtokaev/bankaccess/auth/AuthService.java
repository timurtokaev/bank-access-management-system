package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.auth.dto.AuthTokenResponse;
import com.timurtokaev.bankaccess.auth.dto.LoginRequest;
import com.timurtokaev.bankaccess.common.error.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final TransactionalLoginService transactionalLoginService;

    public AuthService(
            TransactionalLoginService transactionalLoginService
    ) {
        this.transactionalLoginService =
                transactionalLoginService;
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
}