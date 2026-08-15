package com.timurtokaev.bankaccess.rolepermission;

import com.timurtokaev.bankaccess.rolepermission.dto.RolePermissionGrantRequest;
import com.timurtokaev.bankaccess.rolepermission.dto.RolePermissionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(
            RolePermissionService rolePermissionService
    ) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping("/roles/{roleId}/permissions")
    public List<RolePermissionResponse> findAllByRole(
            @PathVariable UUID roleId
    ) {
        return rolePermissionService.findAllByRole(roleId);
    }

    @GetMapping("/permissions/{permissionId}/roles")
    public List<RolePermissionResponse> findAllByPermission(
            @PathVariable UUID permissionId
    ) {
        return rolePermissionService.findAllByPermission(permissionId);
    }

    @PostMapping("/roles/{roleId}/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    public RolePermissionResponse grant(
            @PathVariable UUID roleId,
            @Valid @RequestBody RolePermissionGrantRequest request
    ) {
        return rolePermissionService.grant(roleId, request);
    }

    @DeleteMapping(
            "/roles/{roleId}/permissions/{permissionId}"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        rolePermissionService.revoke(roleId, permissionId);
    }
}