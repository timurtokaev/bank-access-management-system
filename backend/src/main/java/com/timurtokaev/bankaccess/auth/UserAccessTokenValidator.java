package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.user.UserAuthenticationState;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserAccessTokenValidator
        implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TOKEN =
            new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "Access token is invalid",
                    null
            );

    private final UserRepository userRepository;

    public UserAccessTokenValidator(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Optional<UUID> userId = parseUserId(jwt);
        Optional<Long> authVersion = parseAuthVersion(jwt);

        if (userId.isEmpty() || authVersion.isEmpty()) {
            return invalidToken();
        }

        UUID parsedUserId = userId.orElseThrow();
        long parsedAuthVersion = authVersion.orElseThrow();

        Optional<UserAuthenticationState> state =
                userRepository.findAuthenticationStateById(
                        parsedUserId
                );

        if (state.isEmpty()) {
            return invalidToken();
        }

        UserAuthenticationState authenticationState =
                state.orElseThrow();

        if (authenticationState.status() != UserStatus.ACTIVE
                || authenticationState.authVersion()
                        != parsedAuthVersion) {
            return invalidToken();
        }

        return OAuth2TokenValidatorResult.success();
    }

    private Optional<UUID> parseUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    UUID.fromString(jwt.getSubject())
            );
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<Long> parseAuthVersion(Jwt jwt) {
        if (jwt == null) {
            return Optional.empty();
        }

        Object claim = jwt.getClaims().get(
                "auth_version"
        );

        try {
            long value = switch (claim) {
                case Byte number -> number.longValue();
                case Short number -> number.longValue();
                case Integer number -> number.longValue();
                case Long number -> number;
                case BigInteger number -> number.longValueExact();
                case BigDecimal number -> number.longValueExact();
                case null, default -> throw new ArithmeticException();
            };

            return value < 0
                    ? Optional.empty()
                    : Optional.of(value);
        } catch (ArithmeticException exception) {
            return Optional.empty();
        }
    }

    private OAuth2TokenValidatorResult invalidToken() {
        return OAuth2TokenValidatorResult.failure(
                INVALID_TOKEN
        );
    }
}
