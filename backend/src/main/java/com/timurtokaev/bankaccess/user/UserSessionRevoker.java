package com.timurtokaev.bankaccess.user;

import java.util.UUID;

public interface UserSessionRevoker {

    int revokeAllActiveForUser(UUID userId);
}
