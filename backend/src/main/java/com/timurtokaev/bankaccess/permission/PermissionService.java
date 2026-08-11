package com.timurtokaev.bankaccess.permission;

import com.timurtokaev.bankaccess.common.error.ConflictException;
import com.timurtokaev.bankaccess.common.error.ResourceNotFoundException;
import com.timurtokaev.bankaccess.permission.dto.PermissionCreateRequest;
import com.timurtokaev.bankaccess.permission.dto.PermissionResponse;
import com.timurtokaev.bankaccess.permission.dto.PermissionUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(
            PermissionRepository permissionRepository
    ) {
        this.permissionRepository = permissionRepository;
    }

    public List<PermissionResponse> findAll() {
        return permissionRepository
                .findAllByOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PermissionResponse> findAllActive() {
        return permissionRepository
                .findAllByActiveTrueOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PermissionResponse findById(UUID id) {
        return toResponse(getPermission(id));
    }

    public PermissionResponse findByCode(String code) {
        String normalizedCode = normalizeCode(code);

        Permission permission = permissionRepository
                .findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found: " + normalizedCode
                ));

        return toResponse(permission);
    }

    @Transactional
    public PermissionResponse create(
            PermissionCreateRequest request
    ) {
        String code = normalizeCode(request.code());
        String name = normalizeName(request.name());
        String description =
                normalizeDescription(request.description());

        if (permissionRepository.existsByCode(code)) {
            throw new ConflictException(
                    "Permission with code '"
                            + code
                            + "' already exists"
            );
        }

        Permission permission = new Permission(
                code,
                name,
                description
        );

        Permission savedPermission =
                permissionRepository.saveAndFlush(permission);

        return toResponse(savedPermission);
    }

    @Transactional
    public PermissionResponse update(
            UUID id,
            PermissionUpdateRequest request
    ) {
        Permission permission = getPermission(id);

        permission.setName(normalizeName(request.name()));
        permission.setDescription(
                normalizeDescription(request.description())
        );

        Permission savedPermission =
                permissionRepository.saveAndFlush(permission);

        return toResponse(savedPermission);
    }

    @Transactional
    public void deactivate(UUID id) {
        Permission permission = getPermission(id);

        if (!permission.isActive()) {
            return;
        }

        permission.setActive(false);
        permissionRepository.saveAndFlush(permission);
    }

    @Transactional
    public PermissionResponse activate(UUID id) {
        Permission permission = getPermission(id);

        if (!permission.isActive()) {
            permission.setActive(true);
            permissionRepository.saveAndFlush(permission);
        }

        return toResponse(permission);
    }

    private Permission getPermission(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found: " + id
                ));
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Permission code must not be empty"
            );
        }

        String normalizedCode =
                code.trim().toUpperCase(Locale.ROOT);

        if (normalizedCode.length() > 100) {
            throw new IllegalArgumentException(
                    "Permission code must not exceed 100 characters"
            );
        }

        if (!normalizedCode.matches(
                "^[A-Z][A-Z0-9_]*$"
        )) {
            throw new IllegalArgumentException(
                    "Permission code must contain only "
                            + "uppercase letters, digits and underscores"
            );
        }

        return normalizedCode;
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Permission name must not be empty"
            );
        }

        String normalizedName = name.trim();

        if (normalizedName.length() > 150) {
            throw new IllegalArgumentException(
                    "Permission name must not exceed 150 characters"
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
                    "Permission description must not exceed 1000 characters"
            );
        }

        return normalizedDescription;
    }

    private PermissionResponse toResponse(
            Permission permission
    ) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                permission.isActive(),
                permission.getCreatedAt(),
                permission.getUpdatedAt()
        );
    }
}