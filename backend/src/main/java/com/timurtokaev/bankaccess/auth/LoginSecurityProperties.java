package com.timurtokaev.bankaccess.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.login")
public record LoginSecurityProperties(

        int maxFailedAttempts,

        Duration lockDuration

) {

    private static final int MIN_FAILED_ATTEMPTS = 1;
    private static final int MAX_FAILED_ATTEMPTS = 20;

    private static final Duration MIN_LOCK_DURATION =
            Duration.ofMinutes(1);

    private static final Duration MAX_LOCK_DURATION =
            Duration.ofHours(24);

    public LoginSecurityProperties {
        if (maxFailedAttempts < MIN_FAILED_ATTEMPTS
                || maxFailedAttempts > MAX_FAILED_ATTEMPTS) {
            throw new IllegalArgumentException(
                    "Maximum failed login attempts must be between "
                            + MIN_FAILED_ATTEMPTS
                            + " and "
                            + MAX_FAILED_ATTEMPTS
            );
        }

        if (lockDuration == null) {
            throw new IllegalArgumentException(
                    "Login lock duration must not be null"
            );
        }

        if (lockDuration.compareTo(MIN_LOCK_DURATION) < 0
                || lockDuration.compareTo(
                MAX_LOCK_DURATION
        ) > 0) {
            throw new IllegalArgumentException(
                    "Login lock duration must be between "
                            + MIN_LOCK_DURATION
                            + " and "
                            + MAX_LOCK_DURATION
            );
        }
    }
}