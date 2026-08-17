package com.timurtokaev.bankaccess.permission;

import com.timurtokaev.bankaccess.common.error.ConflictException;
import com.timurtokaev.bankaccess.permission.dto.PermissionCreateRequest;
import com.timurtokaev.bankaccess.permission.dto.PermissionResponse;
import com.timurtokaev.bankaccess.permission.dto.PermissionUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    private static final UUID PERMISSION_ID = UUID.fromString(
            "20000000-0000-4000-8000-000000000017"
    );

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private Permission permission;

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService(
                permissionRepository
        );
    }

    @Test
    void shouldRejectSystemPermissionUpdate() {
        stubSystemPermission("PERMISSION_UPDATE");

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> permissionService.update(
                        PERMISSION_ID,
                        new PermissionUpdateRequest(
                                "Renamed permission",
                                "Changed description"
                        )
                )
        );

        assertEquals(
                "System permission cannot be modified: "
                        + "PERMISSION_UPDATE",
                exception.getMessage()
        );

        verify(permission, never()).setName(anyString());
        verify(permission, never()).setDescription(any());
        verify(permissionRepository, never())
                .saveAndFlush(any(Permission.class));
    }

    @Test
    void shouldRejectSystemPermissionDeactivation() {
        stubSystemPermission("PERMISSION_DEACTIVATE");

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> permissionService.deactivate(
                        PERMISSION_ID
                )
        );

        assertEquals(
                "System permission cannot be deactivated: "
                        + "PERMISSION_DEACTIVATE",
                exception.getMessage()
        );

        verify(permission, never()).setActive(false);
        verify(permissionRepository, never())
                .saveAndFlush(any(Permission.class));
    }

    @Test
    void shouldUpdateCustomPermission() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));

        when(permission.getId()).thenReturn(PERMISSION_ID);
        when(permission.getCode()).thenReturn("REPORT_EXPORT");
        when(permission.getName())
                .thenReturn("Export reports");
        when(permission.getDescription())
                .thenReturn("Export approved reports");
        when(permission.isActive()).thenReturn(true);

        when(permissionRepository.saveAndFlush(permission))
                .thenReturn(permission);

        PermissionResponse response = permissionService.update(
                PERMISSION_ID,
                new PermissionUpdateRequest(
                        "  Export reports  ",
                        "  Export approved reports  "
                )
        );

        verify(permission).setName("Export reports");
        verify(permission).setDescription(
                "Export approved reports"
        );
        assertFalse(response.systemPermission());
        assertTrue(response.active());
    }

    @Test
    void shouldDeactivateCustomPermission() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));
        when(permission.isActive()).thenReturn(true);

        permissionService.deactivate(PERMISSION_ID);

        verify(permission).setActive(false);
        verify(permissionRepository).saveAndFlush(permission);
    }

    @Test
    void shouldCreateCustomPermissionAsNonSystem() {
        when(permissionRepository.saveAndFlush(
                any(Permission.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        PermissionResponse response = permissionService.create(
                new PermissionCreateRequest(
                        "REPORT_EXPORT",
                        "Export reports",
                        "Export approved reports"
                )
        );

        assertEquals("REPORT_EXPORT", response.code());
        assertFalse(response.systemPermission());
        assertTrue(response.active());
    }

    @Test
    void shouldAllowSystemPermissionActivationRecovery() {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));
        when(permission.isSystemPermission()).thenReturn(true);
        when(permission.isActive()).thenReturn(false, true);

        PermissionResponse response = permissionService.activate(
                PERMISSION_ID
        );

        verify(permission).setActive(true);
        verify(permissionRepository).saveAndFlush(permission);
        assertTrue(response.systemPermission());
        assertTrue(response.active());
    }

    private void stubSystemPermission(String code) {
        when(permissionRepository.findById(PERMISSION_ID))
                .thenReturn(Optional.of(permission));
        when(permission.isSystemPermission()).thenReturn(true);
        when(permission.getCode()).thenReturn(code);
    }
}
