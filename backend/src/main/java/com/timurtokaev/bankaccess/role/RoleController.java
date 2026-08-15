package com.timurtokaev.bankaccess.role;

import com.timurtokaev.bankaccess.role.dto.RoleCreateRequest;
import com.timurtokaev.bankaccess.role.dto.RoleResponse;
import com.timurtokaev.bankaccess.role.dto.RoleUpdateRequest;
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
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponse> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/active")
    public List<RoleResponse> findAllActive() {
        return roleService.findAllActive();
    }

    @GetMapping("/system")
    public List<RoleResponse> findAllSystemRoles() {
        return roleService.findAllSystemRoles();
    }

    @GetMapping("/{id}")
    public RoleResponse findById(
            @PathVariable UUID id
    ) {
        return roleService.findById(id);
    }

    @GetMapping("/code/{code}")
    public RoleResponse findByCode(
            @PathVariable String code
    ) {
        return roleService.findByCode(code);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(
            @Valid @RequestBody RoleCreateRequest request
    ) {
        return roleService.create(request);
    }

    @PutMapping("/{id}")
    public RoleResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        return roleService.update(id, request);
    }

    @PutMapping("/{id}/activate")
    public RoleResponse activate(
            @PathVariable UUID id
    ) {
        return roleService.activate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable UUID id
    ) {
        roleService.deactivate(id);
    }
}
