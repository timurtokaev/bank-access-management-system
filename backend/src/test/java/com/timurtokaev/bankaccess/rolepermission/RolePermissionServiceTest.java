package com.timurtokaev.bankaccess.rolepermission;

import com.timurtokaev.bankaccess.permission.Permission;
import com.timurtokaev.bankaccess.permission.PermissionRepository;
import com.timurtokaev.bankaccess.role.Role;
import com.timurtokaev.bankaccess.role.RoleRepository;
import com.timurtokaev.bankaccess.rolepermission.dto.RolePermissionGrantRequest;
import com.timurtokaev.bankaccess.rolepermission.dto.RolePermissionResponse;
import com.timurtokaev.bankaccess.userrole.DelegationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {

    private static final UUID ACTOR_USER_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000001"
    );

    private static final UUID ROLE_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000002"
    );

    private static final UUID PERMISSION_ID = UUID.fromString(
            "00000000-0000-4000-8000-000000000003"
    );

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private DelegationPolicy delegationPolicy;

    @Mock
    private Role role;

    @Mock
    private Permission permission;

    private RolePermissionService rolePermissionService;

    @BeforeEach
    void setUp() {
        rolePermissionService = new RolePermissionService(
                rolePermissionRepository,
                roleRepository,
                permissionRepository,
                delegationPolicy
        );
    }

    @Test
    void shouldApplyDelegationPolicyBeforeGrantingPermission() {
        when(roleRepository.findById(ROLE_ID))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        when(role.isSystemRole()).thenReturn(false);
        when(role.isActive()).thenReturn(true);
        when(role.getId()).thenReturn(ROLE_ID);
        when(role.getCode()).thenReturn("REPORT_MANAGER");
        when(role.getName()).thenReturn("Report Manager");

        when(permission.isActive()).thenReturn(true);
        when(permission.getId()).thenReturn(PERMISSION_ID);
        when(permission.getCode()).thenReturn("USER_VIEW");
        when(permission.getName()).thenReturn("View users");

        when(
                rolePermissionRepository.existsById(
                        any(RolePermissionId.class)
                )
        ).thenReturn(false);

        when(
                rolePermissionRepository.saveAndFlush(
                        any(RolePermission.class)
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        RolePermissionResponse response =
                rolePermissionService.grant(
                        ROLE_ID,
                        new RolePermissionGrantRequest(
                                PERMISSION_ID
                        ),
                        ACTOR_USER_ID
                );

        verify(delegationPolicy)
                .requireCanGrantPermission(
                        ACTOR_USER_ID,
                        permission
                );

        assertEquals(ROLE_ID, response.roleId());
        assertEquals(PERMISSION_ID, response.permissionId());
    }
}
