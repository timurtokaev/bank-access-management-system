package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginStateServiceTest {

    private static final UUID USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    private static final OffsetDateTime NOW =
            OffsetDateTime.parse(
                    "2026-08-16T12:00:00Z"
            );

    private static final String PASSWORD_HASH =
            "stored-password-hash";

    private static final String DUMMY_PASSWORD_HASH =
            "dummy-password-hash";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private LoginStateService loginStateService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString()))
                .thenReturn(DUMMY_PASSWORD_HASH);

        LoginSecurityProperties properties =
                new LoginSecurityProperties(
                        5,
                        Duration.ofMinutes(15)
                );

        Clock clock = Clock.fixed(
                NOW.toInstant(),
                ZoneOffset.UTC
        );

        loginStateService = new LoginStateService(
                userRepository,
                passwordEncoder,
                properties,
                clock
        );
    }

    @Test
    void shouldVerifyActiveUserAndResetLoginState() {
        User user = createUser();
        user.setFailedLoginAttempts(3);

        when(
                userRepository.findByUsernameForUpdate(
                        "admin"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "correct-password",
                        PASSWORD_HASH
                )
        ).thenReturn(true);

        Optional<VerifiedLogin> result =
                loginStateService.verify(
                        " ADMIN ",
                        "correct-password"
                );

        assertTrue(result.isPresent());

        assertEquals(
                USER_ID,
                result.orElseThrow().userId()
        );

        assertEquals(
                "admin",
                result.orElseThrow().username()
        );

        assertEquals(
                UserStatus.ACTIVE,
                user.getStatus()
        );

        assertEquals(
                0,
                user.getFailedLoginAttempts()
        );

        assertNull(user.getLockedUntil());

        assertEquals(
                NOW,
                user.getLastLoginAt()
        );

        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void shouldLockUserAfterMaximumFailedAttempts() {
        User user = createUser();
        user.setFailedLoginAttempts(4);

        when(
                userRepository.findByUsernameForUpdate(
                        "admin"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "wrong-password",
                        PASSWORD_HASH
                )
        ).thenReturn(false);

        Optional<VerifiedLogin> result =
                loginStateService.verify(
                        "admin",
                        "wrong-password"
                );

        assertTrue(result.isEmpty());

        assertEquals(
                5,
                user.getFailedLoginAttempts()
        );

        assertEquals(
                UserStatus.LOCKED,
                user.getStatus()
        );

        assertEquals(
                NOW.plusMinutes(15),
                user.getLockedUntil()
        );

        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void shouldPerformDummyCheckForUnknownUser() {
        when(
                userRepository.findByUsernameForUpdate(
                        "missing"
                )
        ).thenReturn(Optional.empty());

        Optional<VerifiedLogin> result =
                loginStateService.verify(
                        "missing",
                        "wrong-password"
                );

        assertTrue(result.isEmpty());

        verify(passwordEncoder).matches(
                "wrong-password",
                DUMMY_PASSWORD_HASH
        );

        verify(
                userRepository,
                never()
        ).saveAndFlush(any(User.class));
    }

    @Test
    void shouldRejectUserWhileTemporaryLockIsActive() {
        User user = createUser();

        user.setStatus(UserStatus.LOCKED);
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(
                NOW.plusMinutes(5)
        );

        when(
                userRepository.findByUsernameForUpdate(
                        "admin"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "correct-password",
                        PASSWORD_HASH
                )
        ).thenReturn(true);

        Optional<VerifiedLogin> result =
                loginStateService.verify(
                        "admin",
                        "correct-password"
                );

        assertTrue(result.isEmpty());

        assertEquals(
                UserStatus.LOCKED,
                user.getStatus()
        );

        verify(
                userRepository,
                never()
        ).saveAndFlush(any(User.class));
    }

    @Test
    void shouldUnlockExpiredTemporaryLockOnSuccess() {
        User user = createUser();

        user.setStatus(UserStatus.LOCKED);
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(
                NOW.minusSeconds(1)
        );

        when(
                userRepository.findByUsernameForUpdate(
                        "admin"
                )
        ).thenReturn(Optional.of(user));

        when(
                passwordEncoder.matches(
                        "correct-password",
                        PASSWORD_HASH
                )
        ).thenReturn(true);

        Optional<VerifiedLogin> result =
                loginStateService.verify(
                        "admin",
                        "correct-password"
                );

        assertTrue(result.isPresent());

        assertEquals(
                UserStatus.ACTIVE,
                user.getStatus()
        );

        assertEquals(
                0,
                user.getFailedLoginAttempts()
        );

        assertNull(user.getLockedUntil());

        assertEquals(
                NOW,
                user.getLastLoginAt()
        );

        verify(userRepository).saveAndFlush(user);
    }

    private User createUser() {
        User user = new User(
                "EMP_001",
                "admin",
                "admin@example.com",
                PASSWORD_HASH,
                "Local",
                "Administrator",
                null
        );

        ReflectionTestUtils.setField(
                user,
                "id",
                USER_ID
        );

        return user;
    }
}