package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginStateService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginSecurityProperties loginProperties;
    private final Clock clock;
    private final String dummyPasswordHash;

    public LoginStateService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            LoginSecurityProperties loginProperties,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginProperties = loginProperties;
        this.clock = clock;

        this.dummyPasswordHash = passwordEncoder.encode(
                "dummy-login-password-"
                        + UUID.randomUUID()
        );
    }

    @Transactional
    public Optional<VerifiedLogin> verify(
            String username,
            String rawPassword
    ) {
        String normalizedUsername =
                normalizeUsername(username);

        String passwordCandidate =
                rawPassword == null
                        ? ""
                        : rawPassword;

        if (normalizedUsername == null) {
            performDummyPasswordCheck(
                    passwordCandidate
            );

            return Optional.empty();
        }

        Optional<User> storedUser =
                userRepository.findByUsernameForUpdate(
                        normalizedUsername
                );

        if (storedUser.isEmpty()) {
            performDummyPasswordCheck(
                    passwordCandidate
            );

            return Optional.empty();
        }

        User user = storedUser.get();

        boolean passwordMatches =
                passwordEncoder.matches(
                        passwordCandidate,
                        user.getPasswordHash()
                );

        OffsetDateTime now = currentTime();

        if (user.getStatus() == UserStatus.INACTIVE) {
            return Optional.empty();
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            OffsetDateTime lockedUntil =
                    user.getLockedUntil();

            if (lockedUntil == null
                    || lockedUntil.isAfter(now)) {
                return Optional.empty();
            }

            user.setStatus(UserStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            return Optional.empty();
        }

        if (!passwordMatches) {
            recordFailedAttempt(user, now);

            return Optional.empty();
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);

        userRepository.saveAndFlush(user);

        return Optional.of(
                new VerifiedLogin(
                        user.getId(),
                        user.getUsername()
                )
        );
    }

    private void recordFailedAttempt(
            User user,
            OffsetDateTime failedAt
    ) {
        int maximumAttempts =
                loginProperties.maxFailedAttempts();

        int currentAttempts = Math.max(
                0,
                user.getFailedLoginAttempts()
        );

        int nextAttempts =
                currentAttempts >= maximumAttempts
                        ? maximumAttempts
                        : currentAttempts + 1;

        user.setFailedLoginAttempts(nextAttempts);

        if (nextAttempts >= maximumAttempts) {
            user.setStatus(UserStatus.LOCKED);

            user.setLockedUntil(
                    failedAt.plus(
                            loginProperties.lockDuration()
                    )
            );
        } else {
            user.setStatus(UserStatus.ACTIVE);
            user.setLockedUntil(null);
        }

        userRepository.saveAndFlush(user);
    }

    private void performDummyPasswordCheck(
            String rawPassword
    ) {
        passwordEncoder.matches(
                rawPassword,
                dummyPasswordHash
        );
    }

    private String normalizeUsername(
            String username
    ) {
        if (username == null || username.isBlank()) {
            return null;
        }

        return username
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private OffsetDateTime currentTime() {
        return OffsetDateTime.ofInstant(
                clock.instant(),
                ZoneOffset.UTC
        );
    }
}