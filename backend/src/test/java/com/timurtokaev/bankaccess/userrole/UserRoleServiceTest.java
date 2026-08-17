package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.role.Role;
import com.timurtokaev.bankaccess.role.RoleRepository;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import com.timurtokaev.bankaccess.userrole.dto.UserRoleAssignRequest;
import com.timurtokaev.bankaccess.userrole.dto.UserRoleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    private static final UUID TARGET_USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    private static final UUID ACTOR_USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000002"
    );

    private static final UUID ROLE_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000003"
    );

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EffectivePermissionService effectivePermissionService;

    @Mock
    private DelegationPolicy delegationPolicy;

    @Mock
    private User targetUser;

    @Mock
    private User actor;

    @Mock
    private Role role;

    private UserRoleService userRoleService;

    @BeforeEach
    void setUp() {
        userRoleService = new UserRoleService(
                userRoleRepository,
                userRepository,
                roleRepository,
                effectivePermissionService,
                delegationPolicy
        );
    }

    @Test
    void shouldPersistAuthenticatedActorAsAssigner() {
        when(userRepository.findById(TARGET_USER_ID))
                .thenReturn(Optional.of(targetUser));

        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.of(role));

        when(targetUser.getStatus())
                .thenReturn(UserStatus.ACTIVE);
        when(targetUser.getId()).thenReturn(TARGET_USER_ID);
        when(targetUser.getEmployeeNumber()).thenReturn("EMP-001");
        when(targetUser.getUsername()).thenReturn("auditor");

        when(role.isActive()).thenReturn(true);
        when(role.getId()).thenReturn(ROLE_ID);
        when(role.getCode()).thenReturn("AUDITOR");
        when(role.getName()).thenReturn("Auditor");

        when(
                delegationPolicy.requireCanAssignRole(
                        ACTOR_USER_ID,
                        role,
                        null
                )
        ).thenReturn(actor);

        when(actor.getId()).thenReturn(ACTOR_USER_ID);
        when(actor.getUsername()).thenReturn("admin");

        when(userRoleRepository.existsById(any(UserRoleId.class)))
                .thenReturn(false);

        when(userRoleRepository.saveAndFlush(any(UserRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserRoleResponse response = userRoleService.assign(
                TARGET_USER_ID,
                new UserRoleAssignRequest(ROLE_ID, null),
                ACTOR_USER_ID
        );

        ArgumentCaptor<UserRole> savedAssignment =
                ArgumentCaptor.forClass(UserRole.class);

        verify(userRoleRepository).saveAndFlush(
                savedAssignment.capture()
        );

        assertSame(
                actor,
                savedAssignment.getValue().getAssignedBy()
        );
        assertEquals(ACTOR_USER_ID, response.assignedById());
        assertEquals("admin", response.assignedByUsername());
    }
}
