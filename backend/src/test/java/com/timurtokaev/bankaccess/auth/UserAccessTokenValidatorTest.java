package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.user.UserAuthenticationState;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccessTokenValidatorTest {

    private static final UUID USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldAcceptActiveUserWithMatchingAuthenticationVersion() {
        when(
                userRepository.findAuthenticationStateById(
                        USER_ID
                )
        ).thenReturn(
                Optional.of(
                        new UserAuthenticationState(
                                UserStatus.ACTIVE,
                                7L
                        )
                )
        );

        OAuth2TokenValidatorResult result = validator()
                .validate(jwt(USER_ID.toString(), 7L));

        assertFalse(result.hasErrors());
    }

    @Test
    void shouldRejectMissingOrMalformedAuthenticationVersion() {
        List<Object> invalidClaims = List.of(
                "7",
                7.0d,
                7.0f,
                -1L,
                new BigDecimal("7.5"),
                BigInteger.valueOf(Long.MAX_VALUE)
                        .add(BigInteger.ONE)
        );

        assertTrue(
                validator().validate(
                        jwtWithoutAuthVersion(
                                USER_ID.toString()
                        )
                ).hasErrors()
        );

        for (Object invalidClaim : invalidClaims) {
            assertTrue(
                    validator().validate(
                            jwt(
                                    USER_ID.toString(),
                                    invalidClaim
                            )
                    ).hasErrors()
            );
        }

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectMissingOrMalformedSubject() {
        assertTrue(
                validator().validate(
                        jwtWithoutSubject(0L)
                ).hasErrors()
        );

        assertTrue(
                validator().validate(
                        jwt("not-a-uuid", 0L)
                ).hasErrors()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectMissingInactiveAndLockedUsers() {
        UserAccessTokenValidator validator = validator();

        when(
                userRepository.findAuthenticationStateById(
                        USER_ID
                )
        ).thenReturn(
                Optional.empty()
        ).thenReturn(
                Optional.of(
                        new UserAuthenticationState(
                                UserStatus.INACTIVE,
                                0L
                        )
                )
        ).thenReturn(
                Optional.of(
                        new UserAuthenticationState(
                                UserStatus.LOCKED,
                                0L
                        )
                )
        );

        assertTrue(
                validator.validate(
                        jwt(USER_ID.toString(), 0L)
                ).hasErrors()
        );

        assertTrue(
                validator.validate(
                        jwt(USER_ID.toString(), 0L)
                ).hasErrors()
        );

        assertTrue(
                validator.validate(
                        jwt(USER_ID.toString(), 0L)
                ).hasErrors()
        );
    }

    @Test
    void shouldRejectAuthenticationVersionMismatchInEitherDirection() {
        when(
                userRepository.findAuthenticationStateById(
                        USER_ID
                )
        ).thenReturn(
                Optional.of(
                        new UserAuthenticationState(
                                UserStatus.ACTIVE,
                                8L
                        )
                )
        ).thenReturn(
                Optional.of(
                        new UserAuthenticationState(
                                UserStatus.ACTIVE,
                                6L
                        )
                )
        );

        UserAccessTokenValidator validator = validator();

        assertTrue(
                validator.validate(
                        jwt(USER_ID.toString(), 7L)
                ).hasErrors()
        );

        assertTrue(
                validator.validate(
                        jwt(USER_ID.toString(), 7L)
                ).hasErrors()
        );
    }

    @Test
    void shouldPropagateRepositoryFailure() {
        when(
                userRepository.findAuthenticationStateById(
                        USER_ID
                )
        ).thenThrow(
                new IllegalStateException("database unavailable")
        );

        assertThrows(
                IllegalStateException.class,
                () -> validator().validate(
                        jwt(USER_ID.toString(), 0L)
                )
        );
    }

    private UserAccessTokenValidator validator() {
        return new UserAccessTokenValidator(userRepository);
    }

    private Jwt jwt(
            String subject,
            Object authVersion
    ) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .claim("sub", subject)
                .claim("auth_version", authVersion)
                .build();
    }

    private Jwt jwtWithoutAuthVersion(String subject) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .claim("sub", subject)
                .build();
    }

    private Jwt jwtWithoutSubject(Object authVersion) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .claim("auth_version", authVersion)
                .build();
    }
}
