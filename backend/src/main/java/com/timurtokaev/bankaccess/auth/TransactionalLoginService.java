package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.auth.dto.AuthTokenResponse;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import com.timurtokaev.bankaccess.userrole.EffectivePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransactionalLoginService {

    private static final String TOKEN_TYPE = "Bearer";

    private final LoginStateService loginStateService;
    private final UserRepository userRepository;
    private final EffectivePermissionService effectivePermissionService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    public TransactionalLoginService(
            LoginStateService loginStateService,
            UserRepository userRepository,
            EffectivePermissionService effectivePermissionService,
            AccessTokenService accessTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.loginStateService = loginStateService;
        this.userRepository = userRepository;
        this.effectivePermissionService =
                effectivePermissionService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public Optional<AuthTokenResponse> execute(
            String username,
            String rawPassword
    ) {
        Optional<VerifiedLogin> verifiedLogin =
                loginStateService.verify(
                        username,
                        rawPassword
                );

        if (verifiedLogin.isEmpty()) {
            return Optional.empty();
        }

        User user = userRepository
                .findById(
                        verifiedLogin.get().userId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Verified user was not found"
                        )
                );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Verified user is not active"
            );
        }

        EffectivePermissionService.Result permissions =
                effectivePermissionService.resolveFor(user);

        IssuedAccessToken accessToken =
                accessTokenService.issue(
                        user.getId(),
                        user.getUsername(),
                        permissions.permissionCodes()
                );

        IssuedRefreshToken refreshToken =
                refreshTokenService.issue(user);

        return Optional.of(
                new AuthTokenResponse(
                        TOKEN_TYPE,
                        accessToken.token(),
                        accessToken.expiresAt(),
                        refreshToken.token(),
                        refreshToken.expiresAt()
                )
        );
    }
}