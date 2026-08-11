package com.timurtokaev.bankaccess.permission;

import com.timurtokaev.bankaccess.permission.dto.PermissionCreateRequest;
import com.timurtokaev.bankaccess.permission.dto.PermissionResponse;
import com.timurtokaev.bankaccess.permission.dto.PermissionUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(
            PermissionService permissionService
    ) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<PermissionResponse> findAll() {
        return permissionService.findAll();
    }

    @GetMapping("/active")
    public List<PermissionResponse> findAllActive() {
        return permissionService.findAllActive();
    }

    @GetMapping("/{id}")
    public PermissionResponse findById(
            @PathVariable UUID id
    ) {
        return permissionService.findById(id);
    }

    @GetMapping("/code/{code}")
    public PermissionResponse findByCode(
            @PathVariable String code
    ) {
        return permissionService.findByCode(code);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse create(
            @Valid @RequestBody PermissionCreateRequest request
    ) {
        return permissionService.create(request);
    }

    @PutMapping("/{id}")
    public PermissionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody PermissionUpdateRequest request
    ) {
        return permissionService.update(id, request);
    }

    @PutMapping("/{id}/activate")
    public PermissionResponse activate(
            @PathVariable UUID id
    ) {
        return permissionService.activate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable UUID id
    ) {
        permissionService.deactivate(id);
    }
}