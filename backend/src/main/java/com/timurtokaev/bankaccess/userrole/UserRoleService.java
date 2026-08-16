package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.common.error.ConflictException;
import com.timurtokaev.bankaccess.common.error.ResourceNotFoundException;
import com.timurtokaev.bankaccess.role.Role;
import com.timurtokaev.bankaccess.role.RoleRepository;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserStatus;
import com.timurtokaev.bankaccess.userrole.dto.UserRoleAssignRequest;
import com.timurtokaev.bankaccess.userrole.dto.UserRoleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.timurtokaev.bankaccess.userrole.dto.UserEffectivePermissionsResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final EffectivePermissionService effectivePermissionService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserRoleService(
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EffectivePermissionService effectivePermissionService
    ) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.effectivePermissionService =
                effectivePermissionService;
    }

    public List<UserRoleResponse> findAllByUser(
            UUID userId
    ) {
        getUser(userId);

        return userRoleRepository
                .findAllByUser_IdOrderByRole_CodeAsc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<UserRoleResponse> findAllByRole(
            UUID roleId
    ) {
        getRole(roleId);

        return userRoleRepository
                .findAllByRole_IdOrderByUser_LastNameAscUser_FirstNameAsc(
                        roleId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }
    public UserEffectivePermissionsResponse findEffectivePermissions(
            UUID userId
    ) {
        User user = getUser(userId);

        EffectivePermissionService.Result result =
                effectivePermissionService.resolveFor(user);

        return new UserEffectivePermissionsResponse(
                user.getId(),
                user.getEmployeeNumber(),
                user.getUsername(),
                user.getStatus(),
                result.evaluatedAt(),
                result.permissionCodes()
        );
    }
    @Transactional
    public UserRoleResponse assign(
            UUID userId,
            UserRoleAssignRequest request
    ) {
        User user = getUser(userId);
        Role role = getRole(request.roleId());

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ConflictException(
                    "Cannot assign roles to user with status "
                            + user.getStatus()
                            + ": "
                            + user.getUsername()
            );
        }

        if (!role.isActive()) {
            throw new ConflictException(
                    "Cannot assign inactive role: "
                            + role.getCode()
            );
        }

        OffsetDateTime assignedAt =
                OffsetDateTime.now(ZoneOffset.UTC);

        OffsetDateTime expiresAt =
                normalizeExpiration(
                        request.expiresAt(),
                        assignedAt
                );

        UserRoleId id = new UserRoleId(
                user.getId(),
                role.getId()
        );

        if (userRoleRepository.existsById(id)) {
            throw new ConflictException(
                    "Role '"
                            + role.getCode()
                            + "' is already assigned to user '"
                            + user.getUsername()
                            + "'"
            );
        }

        UserRole userRole = new UserRole(
                user,
                role,
                null,
                assignedAt,
                expiresAt
        );

        UserRole savedUserRole =
                userRoleRepository.saveAndFlush(userRole);

        return toResponse(savedUserRole);
    }

    @Transactional
    public void revoke(
            UUID userId,
            UUID roleId
    ) {
        getUser(userId);

        UserRoleId id = new UserRoleId(
                userId,
                roleId
        );

        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role assignment not found: "
                                        + userId
                                        + " / "
                                        + roleId
                        )
                );

        userRoleRepository.delete(userRole);
        userRoleRepository.flush();
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + id
                        )
                );
    }

    private Role getRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found: " + id
                        )
                );
    }

    private OffsetDateTime normalizeExpiration(
            OffsetDateTime expiresAt,
            OffsetDateTime assignedAt
    ) {
        if (expiresAt == null) {
            return null;
        }

        OffsetDateTime normalizedExpiration =
                expiresAt.withOffsetSameInstant(ZoneOffset.UTC);

        if (!normalizedExpiration.isAfter(assignedAt)) {
            throw new ConflictException(
                    "Role expiration time must be in the future"
            );
        }

        return normalizedExpiration;
    }

    private UserRoleResponse toResponse(
            UserRole userRole
    ) {
        User user = userRole.getUser();
        Role role = userRole.getRole();
        User assignedBy = userRole.getAssignedBy();

        return new UserRoleResponse(
                user.getId(),
                user.getEmployeeNumber(),
                user.getUsername(),
                role.getId(),
                role.getCode(),
                role.getName(),
                assignedBy == null
                        ? null
                        : assignedBy.getId(),
                assignedBy == null
                        ? null
                        : assignedBy.getUsername(),
                userRole.getAssignedAt(),
                userRole.getExpiresAt()
        );
    }
}