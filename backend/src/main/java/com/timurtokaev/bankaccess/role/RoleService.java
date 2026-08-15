package com.timurtokaev.bankaccess.role;

import com.timurtokaev.bankaccess.common.error.ConflictException;
import com.timurtokaev.bankaccess.common.error.ResourceNotFoundException;
import com.timurtokaev.bankaccess.role.dto.RoleCreateRequest;
import com.timurtokaev.bankaccess.role.dto.RoleResponse;
import com.timurtokaev.bankaccess.role.dto.RoleUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleResponse> findAll() {
        return roleRepository
                .findAllByOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RoleResponse> findAllActive() {
        return roleRepository
                .findAllByActiveTrueOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RoleResponse> findAllSystemRoles() {
        return roleRepository
                .findAllBySystemRoleTrueOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponse findById(UUID id) {
        return toResponse(getRole(id));
    }

    public RoleResponse findByCode(String code) {
        String normalizedCode = normalizeCode(code);

        Role role = roleRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + normalizedCode
                ));

        return toResponse(role);
    }

    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        String code = normalizeCode(request.code());
        String name = normalizeName(request.name());
        String description =
                normalizeDescription(request.description());

        if (roleRepository.existsByCode(code)) {
            throw new ConflictException(
                    "Role with code '"
                            + code
                            + "' already exists"
            );
        }

        Role role = new Role(
                code,
                name,
                description
        );

        Role savedRole = roleRepository.saveAndFlush(role);

        return toResponse(savedRole);
    }

    @Transactional
    public RoleResponse update(
            UUID id,
            RoleUpdateRequest request
    ) {
        Role role = getRole(id);

        ensureNotSystemRole(
                role,
                "System role cannot be modified: "
        );

        role.setName(normalizeName(request.name()));
        role.setDescription(
                normalizeDescription(request.description())
        );

        Role savedRole = roleRepository.saveAndFlush(role);

        return toResponse(savedRole);
    }

    @Transactional
    public void deactivate(UUID id) {
        Role role = getRole(id);

        ensureNotSystemRole(
                role,
                "System role cannot be deactivated: "
        );

        if (!role.isActive()) {
            return;
        }

        role.setActive(false);
        roleRepository.saveAndFlush(role);
    }

    @Transactional
    public RoleResponse activate(UUID id) {
        Role role = getRole(id);

        if (!role.isActive()) {
            role.setActive(true);
            roleRepository.saveAndFlush(role);
        }

        return toResponse(role);
    }

    private Role getRole(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found: " + id
                ));
    }

    private void ensureNotSystemRole(
            Role role,
            String message
    ) {
        if (role.isSystemRole()) {
            throw new ConflictException(
                    message + role.getCode()
            );
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Role code must not be empty"
            );
        }

        String normalizedCode =
                code.trim().toUpperCase(Locale.ROOT);

        if (normalizedCode.length() > 100) {
            throw new IllegalArgumentException(
                    "Role code must not exceed 100 characters"
            );
        }

        if (!normalizedCode.matches(
                "^[A-Z][A-Z0-9_]*$"
        )) {
            throw new IllegalArgumentException(
                    "Role code must contain only "
                            + "uppercase letters, digits and underscores"
            );
        }

        return normalizedCode;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Role name must not be empty"
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException(
                    "Role name must not exceed 150 characters"
            );
        }

        return normalizedName;
    }

    private String normalizeDescription(
            String description
    ) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String normalizedDescription = description.trim();

        if (normalizedDescription.length() > 1000) {
            throw new IllegalArgumentException(
                    "Role description must not exceed 1000 characters"
            );
        }

        return normalizedDescription;
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}