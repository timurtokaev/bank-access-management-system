package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.permission.Permission;
import com.timurtokaev.bankaccess.role.Role;
import com.timurtokaev.bankaccess.rolepermission.RolePermissionRepository;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelegationPolicyTest {

    private static final UUID ACTOR_USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    private static final UUID ROLE_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000002"
    );

    private static final OffsetDateTime NOW =
            OffsetDateTime.parse("2026-08-17T12:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private EffectivePermissionService effectivePermissionService;

    @Mock
    private User actor;

    @Mock
    private Role role;

    @Mock
    private Permission permission;

    private DelegationPolicy delegationPolicy;

    @BeforeEach
    void setUp() {
        delegationPolicy = new DelegationPolicy(
                userRepository,
                userRoleRepository,
                rolePermissionRepository,
                effectivePermissionService
        );
    }

    @Test
    void shouldRejectAdminRoleAssignmentByNonAdmin() {
        stubActor(
                List.of(
                        "ROLE_ASSIGN",
                        "USER_VIEW"
                ),
                false
        );
        stubUnboundedPermission("ROLE_ASSIGN");

        when(role.getCode()).thenReturn("ADMIN");

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanAssignRole(
                        ACTOR_USER_ID,
                        role,
                        null
                )
        );

        verifyNoInteractions(rolePermissionRepository);
    }

    @Test
    void shouldRejectRoleWithPermissionActorDoesNotHave() {
        stubActor(
                List.of(
                        "ROLE_ASSIGN",
                        "USER_VIEW"
                ),
                false
        );
        stubUnboundedPermission("ROLE_ASSIGN");

        stubRole("REPORT_MANAGER");

        when(
                rolePermissionRepository
                        .findPermissionCodesByRoleId(ROLE_ID)
        ).thenReturn(
                List.of(
                        "DEPARTMENT_UPDATE",
                        "USER_VIEW"
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanAssignRole(
                        ACTOR_USER_ID,
                        role,
                        null
                )
        );
    }

    @Test
    void shouldAllowRoleWhosePermissionsAreWithinActorCeiling() {
        stubActor(
                List.of(
                        "ROLE_ASSIGN",
                        "USER_VIEW"
                ),
                false
        );
        stubUnboundedPermission("ROLE_ASSIGN");
        stubUnboundedPermission("USER_VIEW");

        stubRole("REPORT_VIEWER");

        when(
                rolePermissionRepository
                        .findPermissionCodesByRoleId(ROLE_ID)
        ).thenReturn(List.of("USER_VIEW"));

        User result = delegationPolicy.requireCanAssignRole(
                ACTOR_USER_ID,
                role,
                null
        );

        assertSame(actor, result);
    }

    @Test
    void shouldRejectPermanentRoleAssignmentFromTemporaryRoleAssignPrivilege() {
        stubActor(
                List.of("ROLE_ASSIGN"),
                false
        );
        stubPermissionExpiration(
                "ROLE_ASSIGN",
                NOW.plusHours(1)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanAssignRole(
                        ACTOR_USER_ID,
                        role,
                        null
                )
        );

        verifyNoInteractions(rolePermissionRepository);
    }

    @Test
    void shouldRejectPermanentAdminAssignmentByTemporaryAdmin() {
        stubActor(
                List.of("ROLE_ASSIGN"),
                List.of(assignmentExpiringAt(NOW.plusHours(1)))
        );
        stubUnboundedPermission("ROLE_ASSIGN");

        when(role.getCode()).thenReturn("ADMIN");

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanAssignRole(
                        ACTOR_USER_ID,
                        role,
                        null
                )
        );

        verifyNoInteractions(rolePermissionRepository);
    }

    @Test
    void shouldAllowAdminAssignmentAtTemporaryAdminHorizon() {
        OffsetDateTime administratorExpiresAt =
                NOW.plusHours(1);

        stubActor(
                List.of("ROLE_ASSIGN"),
                List.of(assignmentExpiringAt(
                        administratorExpiresAt
                ))
        );
        stubUnboundedPermission("ROLE_ASSIGN");

        User result = delegationPolicy.requireCanAssignRole(
                ACTOR_USER_ID,
                role,
                administratorExpiresAt
        );

        assertSame(actor, result);
        verifyNoInteractions(rolePermissionRepository);
    }

    @Test
    void shouldRequireCurrentRoleAssignPermission() {
        stubActor(
                List.of("USER_VIEW"),
                true
        );

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanAssignRole(
                        ACTOR_USER_ID,
                        role,
                        null
                )
        );

        verifyNoInteractions(rolePermissionRepository);
    }

    @Test
    void shouldRejectGrantOfPermissionActorDoesNotHave() {
        stubActor(
                List.of("PERMISSION_GRANT"),
                false
        );
        stubUnboundedPermission("PERMISSION_GRANT");

        when(permission.getCode())
                .thenReturn("DEPARTMENT_UPDATE");

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanGrantPermission(
                        ACTOR_USER_ID,
                        permission
                )
        );
    }

    @Test
    void shouldAllowSystemAdminToGrantNewPermission() {
        stubActor(
                List.of("PERMISSION_GRANT"),
                true
        );
        stubUnboundedPermission("PERMISSION_GRANT");

        delegationPolicy.requireCanGrantPermission(
                ACTOR_USER_ID,
                permission
        );
    }

    @Test
    void shouldRejectAdminRoleRevocationByNonAdmin() {
        stubActor(
                List.of("ROLE_REVOKE"),
                false
        );

        when(role.getCode()).thenReturn("ADMIN");

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanRevokeRole(
                        ACTOR_USER_ID,
                        role
                )
        );
    }

    @Test
    void shouldRejectPermissionRevocationAboveActorCeiling() {
        stubActor(
                List.of("PERMISSION_REVOKE"),
                false
        );

        when(permission.getCode())
                .thenReturn("DEPARTMENT_UPDATE");

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanRevokePermission(
                        ACTOR_USER_ID,
                        permission
                )
        );
    }

    @Test
    void shouldRejectPermanentRoleAssignmentWhenDelegatedPermissionIsTemporary() {
        OffsetDateTime privilegeExpiresAt = NOW.plusHours(1);

        stubActor(
                List.of(
                        "ROLE_ASSIGN",
                        "USER_VIEW"
                ),
                false
        );
        stubRole("REPORT_VIEWER");
        stubUnboundedPermission("ROLE_ASSIGN");
        stubPermissionExpiration(
                "USER_VIEW",
                privilegeExpiresAt
        );

        when(
                rolePermissionRepository
                        .findPermissionCodesByRoleId(ROLE_ID)
        ).thenReturn(List.of("USER_VIEW"));

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanAssignRole(
                        ACTOR_USER_ID,
                        role,
                        null
                )
        );
    }

    @Test
    void shouldAllowRoleAssignmentWithinTemporaryPrivilegeHorizon() {
        OffsetDateTime privilegeExpiresAt = NOW.plusHours(1);

        stubActor(
                List.of(
                        "ROLE_ASSIGN",
                        "USER_VIEW"
                ),
                false
        );
        stubRole("REPORT_VIEWER");
        stubPermissionExpiration(
                "ROLE_ASSIGN",
                privilegeExpiresAt
        );
        stubPermissionExpiration(
                "USER_VIEW",
                privilegeExpiresAt
        );

        when(
                rolePermissionRepository
                        .findPermissionCodesByRoleId(ROLE_ID)
        ).thenReturn(List.of("USER_VIEW"));

        User result = delegationPolicy.requireCanAssignRole(
                ACTOR_USER_ID,
                role,
                NOW.plusMinutes(30)
        );

        assertSame(actor, result);
    }

    @Test
    void shouldRejectPermanentGrantFromTemporaryGrantPrivilege() {
        stubActor(
                List.of(
                        "PERMISSION_GRANT",
                        "DEPARTMENT_UPDATE"
                ),
                false
        );
        stubPermissionExpiration(
                "PERMISSION_GRANT",
                NOW.plusHours(1)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanGrantPermission(
                        ACTOR_USER_ID,
                        permission
                )
        );
    }

    @Test
    void shouldRejectPermanentGrantOfTemporarilyHeldPermission() {
        stubActor(
                List.of(
                        "PERMISSION_GRANT",
                        "DEPARTMENT_UPDATE"
                ),
                false
        );
        stubUnboundedPermission("PERMISSION_GRANT");
        stubPermissionExpiration(
                "DEPARTMENT_UPDATE",
                NOW.plusHours(1)
        );

        when(permission.getCode())
                .thenReturn("DEPARTMENT_UPDATE");

        assertThrows(
                AccessDeniedException.class,
                () -> delegationPolicy.requireCanGrantPermission(
                        ACTOR_USER_ID,
                        permission
                )
        );
    }

    private void stubActor(
            List<String> permissionCodes,
            boolean systemAdministrator
    ) {
        stubActor(
                permissionCodes,
                systemAdministrator
                        ? List.of(mock(UserRole.class))
                        : List.of()
        );
    }

    private void stubActor(
            List<String> permissionCodes,
            List<UserRole> administratorAssignments
    ) {
        when(userRepository.findById(ACTOR_USER_ID))
                .thenReturn(Optional.of(actor));

        when(actor.getStatus())
                .thenReturn(UserStatus.ACTIVE);
        when(effectivePermissionService.resolveFor(actor))
                .thenReturn(
                        new EffectivePermissionService.Result(
                                NOW,
                                permissionCodes
                        )
                );

        when(
                userRoleRepository
                        .findEffectiveSystemRoleAssignments(
                                ACTOR_USER_ID,
                                UserStatus.ACTIVE,
                                "ADMIN",
                                NOW
                        )
        ).thenReturn(
                administratorAssignments
        );

    }

    private void stubUnboundedPermission(
            String permissionCode
    ) {
        when(actor.getId())
                .thenReturn(ACTOR_USER_ID);

        when(
                userRoleRepository
                            .findEffectiveRoleAssignmentsProvidingPermission(
                                ACTOR_USER_ID,
                                UserStatus.ACTIVE,
                                permissionCode,
                                NOW
                        )
        ).thenReturn(List.of(mock(UserRole.class)));
    }

    private void stubPermissionExpiration(
            String permissionCode,
            OffsetDateTime expiration
    ) {
        UserRole assignment = mock(UserRole.class);

        when(actor.getId())
                .thenReturn(ACTOR_USER_ID);

        when(assignment.getExpiresAt())
                .thenReturn(expiration);

        when(
                userRoleRepository
                        .findEffectiveRoleAssignmentsProvidingPermission(
                                ACTOR_USER_ID,
                                UserStatus.ACTIVE,
                                permissionCode,
                                NOW
                        )
        ).thenReturn(List.of(assignment));
    }

    private UserRole assignmentExpiringAt(
            OffsetDateTime expiration
    ) {
        UserRole assignment = mock(UserRole.class);

        when(assignment.getExpiresAt())
                .thenReturn(expiration);

        return assignment;
    }

    private void stubRole(String code) {
        when(role.getCode()).thenReturn(code);
        when(role.getId()).thenReturn(ROLE_ID);
    }
}
