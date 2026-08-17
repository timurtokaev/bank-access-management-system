package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.permission.Permission;
import com.timurtokaev.bankaccess.role.Role;
import com.timurtokaev.bankaccess.rolepermission.RolePermissionRepository;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DelegationPolicy {

    private static final String ADMIN_ROLE_CODE = "ADMIN";
    private static final String ROLE_ASSIGN_PERMISSION = "ROLE_ASSIGN";
    private static final String ROLE_REVOKE_PERMISSION = "ROLE_REVOKE";
    private static final String PERMISSION_GRANT_PERMISSION =
            "PERMISSION_GRANT";
    private static final String PERMISSION_REVOKE_PERMISSION =
            "PERMISSION_REVOKE";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final EffectivePermissionService effectivePermissionService;

    public DelegationPolicy(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            EffectivePermissionService effectivePermissionService
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.effectivePermissionService = effectivePermissionService;
    }

    public User requireCanAssignRole(
            UUID actorUserId,
            Role role,
            OffsetDateTime assignmentExpiresAt
    ) {
        Objects.requireNonNull(
                role,
                "Role must not be null"
        );

        OffsetDateTime delegatedUntil = normalizeExpiration(
                assignmentExpiresAt
        );

        ActorCapabilities actor = resolveActor(actorUserId);

        requirePermissionThrough(
                actor,
                ROLE_ASSIGN_PERMISSION,
                delegatedUntil
        );

        if (actor.systemAdministrator()
                && isAuthorizedThrough(
                actor.systemAdministratorAssignments(),
                delegatedUntil
        )) {
            return actor.user();
        }

        if (ADMIN_ROLE_CODE.equals(role.getCode())) {
            throw delegationDenied();
        }

        for (String permissionCode : rolePermissionRepository
                .findPermissionCodesByRoleId(
                        requireRoleId(role)
                )) {
            requirePermissionThrough(
                    actor,
                    permissionCode,
                    delegatedUntil
            );
        }

        return actor.user();
    }

    public void requireCanRevokeRole(
            UUID actorUserId,
            Role role
    ) {
        Objects.requireNonNull(
                role,
                "Role must not be null"
        );

        ActorCapabilities actor = resolveActor(actorUserId);

        requireCurrentPermission(
                actor,
                ROLE_REVOKE_PERMISSION
        );

        if (actor.systemAdministrator()) {
            return;
        }

        if (ADMIN_ROLE_CODE.equals(role.getCode())) {
            throw delegationDenied();
        }

        List<String> delegatedPermissions =
                rolePermissionRepository
                        .findPermissionCodesByRoleId(
                                requireRoleId(role)
                        );

        if (!actor.permissionCodes().containsAll(
                delegatedPermissions
        )) {
            throw delegationDenied();
        }
    }

    public void requireCanGrantPermission(
            UUID actorUserId,
            Permission permission
    ) {
        Objects.requireNonNull(
                permission,
                "Permission must not be null"
        );

        ActorCapabilities actor = resolveActor(actorUserId);

        requirePermissionThrough(
                actor,
                PERMISSION_GRANT_PERMISSION,
                null
        );

        if (actor.systemAdministrator()
                && isAuthorizedThrough(
                actor.systemAdministratorAssignments(),
                null
        )) {
            return;
        }

        requirePermissionThrough(
                actor,
                permission.getCode(),
                null
        );
    }

    public void requireCanRevokePermission(
            UUID actorUserId,
            Permission permission
    ) {
        Objects.requireNonNull(
                permission,
                "Permission must not be null"
        );

        ActorCapabilities actor = resolveActor(actorUserId);

        requireCurrentPermission(
                actor,
                PERMISSION_REVOKE_PERMISSION
        );

        if (actor.systemAdministrator()) {
            return;
        }

        if (!actor.permissionCodes().contains(
                permission.getCode()
        )) {
            throw delegationDenied();
        }
    }

    private ActorCapabilities resolveActor(
            UUID actorUserId
    ) {
        Objects.requireNonNull(
                actorUserId,
                "Actor user ID must not be null"
        );

        User actor = userRepository.findById(actorUserId)
                .filter(user ->
                        user.getStatus() == UserStatus.ACTIVE
                )
                .orElseThrow(this::delegationDenied);

        EffectivePermissionService.Result permissions =
                effectivePermissionService.resolveFor(actor);

        List<UserRole> administratorAssignments =
                userRoleRepository
                        .findEffectiveSystemRoleAssignments(
                                actorUserId,
                                UserStatus.ACTIVE,
                                ADMIN_ROLE_CODE,
                                permissions.evaluatedAt()
                        );

        return new ActorCapabilities(
                actor,
                Set.copyOf(permissions.permissionCodes()),
                permissions.evaluatedAt(),
                List.copyOf(administratorAssignments),
                !administratorAssignments.isEmpty()
        );
    }

    private UUID requireRoleId(Role role) {
        return Objects.requireNonNull(
                role.getId(),
                "Role ID must not be null"
        );
    }

    private void requireCurrentPermission(
            ActorCapabilities actor,
            String permissionCode
    ) {
        if (!actor.permissionCodes().contains(permissionCode)) {
            throw delegationDenied();
        }
    }

    private void requirePermissionThrough(
            ActorCapabilities actor,
            String permissionCode,
            OffsetDateTime delegatedUntil
    ) {
        requireCurrentPermission(actor, permissionCode);

        List<UserRole> assignments =
                userRoleRepository
                        .findEffectiveRoleAssignmentsProvidingPermission(
                                actor.user().getId(),
                                UserStatus.ACTIVE,
                                permissionCode,
                                actor.evaluatedAt()
                        );

        if (!isAuthorizedThrough(
                assignments,
                delegatedUntil
        )) {
            throw delegationDenied();
        }
    }

    private boolean isAuthorizedThrough(
            List<UserRole> assignments,
            OffsetDateTime delegatedUntil
    ) {
        if (assignments.stream()
                .map(UserRole::getExpiresAt)
                .anyMatch(Objects::isNull)) {
            return true;
        }

        if (delegatedUntil == null) {
            return false;
        }

        return assignments.stream()
                .map(UserRole::getExpiresAt)
                .max(Comparator.naturalOrder())
                .map(expiration ->
                        !delegatedUntil.isAfter(expiration)
                )
                .orElse(false);
    }

    private OffsetDateTime normalizeExpiration(
            OffsetDateTime expiration
    ) {
        return expiration == null
                ? null
                : expiration.withOffsetSameInstant(
                        ZoneOffset.UTC
                );
    }

    private AccessDeniedException delegationDenied() {
        return new AccessDeniedException(
                "Delegation exceeds the authenticated user's current privileges"
        );
    }

    private record ActorCapabilities(
            User user,
            Set<String> permissionCodes,
            OffsetDateTime evaluatedAt,
            List<UserRole> systemAdministratorAssignments,
            boolean systemAdministrator
    ) {
    }
}
