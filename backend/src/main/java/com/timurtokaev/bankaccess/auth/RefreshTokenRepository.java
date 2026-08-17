package com.timurtokaev.bankaccess.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    @Query("""
            SELECT refreshToken.user.id
            FROM RefreshToken refreshToken
            WHERE refreshToken.tokenHash = :tokenHash
            """)
    Optional<UUID> findUserIdByTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT refreshToken
            FROM RefreshToken refreshToken
            JOIN FETCH refreshToken.user
            WHERE refreshToken.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Modifying(
            flushAutomatically = true
    )
    @Query("""
            UPDATE RefreshToken refreshToken
            SET refreshToken.revokedAt = :revokedAt
            WHERE refreshToken.user.id = :userId
              AND refreshToken.revokedAt IS NULL
              AND refreshToken.expiresAt > :revokedAt
            """)
    int revokeAllActiveByUserId(
            @Param("userId") UUID userId,
            @Param("revokedAt") OffsetDateTime revokedAt
    );
}
