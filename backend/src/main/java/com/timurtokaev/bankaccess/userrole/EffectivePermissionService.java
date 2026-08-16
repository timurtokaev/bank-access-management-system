package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EffectivePermissionService {

    private final UserRoleRepository userRoleRepository;
    private final Clock clock;

    public EffectivePermissionService(
            UserRoleRepository userRoleRepository,
            Clock clock
    ) {
        this.userRoleRepository = userRoleRepository;
        this.clock = clock;
    }

    public Result resolveFor(User user) {
        Objects.requireNonNull(
                user,
                "User must not be null"
        );

        UUID userId = Objects.requireNonNull(
                user.getId(),
                "User ID must not be null"
        );

        OffsetDateTime evaluatedAt =
                OffsetDateTime.ofInstant(
                        clock.instant(),
                        ZoneOffset.UTC
                );

        List<String> permissionCodes;

        if (user.getStatus() == UserStatus.ACTIVE) {
            permissionCodes =
                    userRoleRepository.findEffectivePermissionCodes(
                            userId,
                            UserStatus.ACTIVE,
                            evaluatedAt
                    );
        } else {
            permissionCodes = List.of();
        }

        return new Result(
                evaluatedAt,
                permissionCodes
        );
    }

    public record Result(

            OffsetDateTime evaluatedAt,

            List<String> permissionCodes

    ) {

        public Result {
            Objects.requireNonNull(
                    evaluatedAt,
                    "Evaluation time must not be null"
            );

            evaluatedAt = evaluatedAt.withOffsetSameInstant(
                    ZoneOffset.UTC
            );

            permissionCodes = List.copyOf(
                    Objects.requireNonNull(
                            permissionCodes,
                            "Permission codes must not be null"
                    )
            );
        }
    }
}