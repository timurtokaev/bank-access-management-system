package com.timurtokaev.bankaccess.user;

public record UserAuthenticationState(
        UserStatus status,
        long authVersion
) {
}
