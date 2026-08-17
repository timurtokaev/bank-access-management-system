package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.auth.dto.AuthTokenResponse;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import com.timurtokaev.bankaccess.userrole.EffectivePermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionalRefreshService {

    private static final String TOKEN_TYPE = "Bearer";

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final EffectivePermissionService effectivePermissionService;
    private final AccessTokenService accessTokenService;

    public TransactionalRefreshService(
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            EffectivePermissionService effectivePermissionService,
            AccessTokenService accessTokenService
    ) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.effectivePermissionService =
                effectivePermissionService;
        this.accessTokenService = accessTokenService;
    }

    @Transactional
    public AuthTokenResponse execute(
            String rawRefreshToken
    ) {
        RotatedRefreshToken rotatedRefreshToken =
                refreshTokenService.rotate(
                        rawRefreshToken
                );

        User user = userRepository
                .findById(
                        rotatedRefreshToken.userId()
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Rotated refresh token user was not found"
                        )
                );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Rotated refresh token user is not active"
            );
        }

        EffectivePermissionService.Result permissions =
                effectivePermissionService.resolveFor(user);

        IssuedAccessToken accessToken =
                accessTokenService.issue(
                        user.getId(),
                        user.getUsername(),
                        permissions.permissionCodes(),
                        user.getAuthVersion()
                );

        IssuedRefreshToken refreshToken =
                rotatedRefreshToken.issuedToken();

        return new AuthTokenResponse(
                TOKEN_TYPE,
                accessToken.token(),
                accessToken.expiresAt(),
                refreshToken.token(),
                refreshToken.expiresAt()
        );
    }
}
