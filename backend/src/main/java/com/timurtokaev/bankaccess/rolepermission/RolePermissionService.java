package com.timurtokaev.bankaccess.rolepermission;

import com.timurtokaev.bankaccess.common.error.ConflictException;
import com.timurtokaev.bankaccess.common.error.ResourceNotFoundException;
import com.timurtokaev.bankaccess.permission.Permission;
import com.timurtokaev.bankaccess.permission.PermissionRepository;
import com.timurtokaev.bankaccess.role.Role;
import com.timurtokaev.bankaccess.role.RoleRepository;
import com.timurtokaev.bankaccess.rolepermission.dto.RolePermissionGrantRequest;
import com.timurtokaev.bankaccess.rolepermission.dto.RolePermissionResponse;
import com.timurtokaev.bankaccess.userrole.DelegationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final DelegationPolicy delegationPolicy;

    public RolePermissionService(
            RolePermissionRepository rolePermissionRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            DelegationPolicy delegationPolicy
    ) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.delegationPolicy = delegationPolicy;
    }

    public List<RolePermissionResponse> findAllByRole(
            UUID roleId
    ) {
        getRole(roleId);

        return rolePermissionRepository
                .findAllByRole_IdOrderByPermission_CodeAsc(roleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RolePermissionResponse> findAllByPermission(
            UUID permissionId
    ) {
        getPermission(permissionId);

        return rolePermissionRepository
                .findAllByPermission_IdOrderByRole_CodeAsc(
                        permissionId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RolePermissionResponse grant(
            UUID roleId,
            RolePermissionGrantRequest request,
            UUID actorUserId
    ) {
        Role role = getRole(roleId);
        Permission permission =
                getPermission(request.permissionId());

        ensureRoleCanBeModified(role);

        if (!role.isActive()) {
            throw new ConflictException(
                    "Cannot grant permissions to inactive role: "
                            + role.getCode()
            );
        }

        if (!permission.isActive()) {
            throw new ConflictException(
                    "Cannot grant inactive permission: "
                            + permission.getCode()
            );
        }

        delegationPolicy.requireCanGrantPermission(
                actorUserId,
                permission
        );

        RolePermissionId id = new RolePermissionId(
                role.getId(),
                permission.getId()
        );

        if (rolePermissionRepository.existsById(id)) {
            throw new ConflictException(
                    "Permission '"
                            + permission.getCode()
                            + "' is already granted to role '"
                            + role.getCode()
                            + "'"
            );
        }

        RolePermission rolePermission =
                new RolePermission(role, permission);

        RolePermission savedRolePermission =
                rolePermissionRepository.saveAndFlush(
                        rolePermission
                );

        return toResponse(savedRolePermission);
    }

    @Transactional
    public void revoke(
            UUID roleId,
            UUID permissionId,
            UUID actorUserId
    ) {
        Role role = getRole(roleId);

        ensureRoleCanBeModified(role);

        RolePermissionId id = new RolePermissionId(
                roleId,
                permissionId
        );

        RolePermission rolePermission =
                rolePermissionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Permission assignment not found: "
                                                + roleId
                                                + " / "
                                                + permissionId
                                )
                        );

        delegationPolicy.requireCanRevokePermission(
                actorUserId,
                rolePermission.getPermission()
        );

        rolePermissionRepository.delete(rolePermission);
        rolePermissionRepository.flush();
    }

    private Role getRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + id
                ));
    }

    private Permission getPermission(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found: " + id
                ));
    }

    private void ensureRoleCanBeModified(Role role) {
        if (role.isSystemRole()) {
            throw new ConflictException(
                    "Permissions of system role cannot be modified: "
                            + role.getCode()
            );
        }
    }

    private RolePermissionResponse toResponse(
            RolePermission rolePermission
    ) {
        Role role = rolePermission.getRole();
        Permission permission =
                rolePermission.getPermission();

        return new RolePermissionResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                rolePermission.getGrantedAt()
        );
    }
}
