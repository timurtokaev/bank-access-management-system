package com.timurtokaev.bankaccess.userrole;

import com.timurtokaev.bankaccess.userrole.dto.UserRoleAssignRequest;
import com.timurtokaev.bankaccess.userrole.dto.UserRoleResponse;
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
import com.timurtokaev.bankaccess.userrole.dto.UserEffectivePermissionsResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(
            UserRoleService userRoleService
    ) {
        this.userRoleService = userRoleService;
    }

    @GetMapping("/users/{userId}/roles")
    public List<UserRoleResponse> findAllByUser(
            @PathVariable UUID userId
    ) {
        return userRoleService.findAllByUser(userId);
    }
    @GetMapping("/users/{userId}/effective-permissions")
    public UserEffectivePermissionsResponse findEffectivePermissions(
            @PathVariable UUID userId
    ) {
        return userRoleService.findEffectivePermissions(userId);
    }
    @GetMapping("/roles/{roleId}/users")
    public List<UserRoleResponse> findAllByRole(
            @PathVariable UUID roleId
    ) {
        return userRoleService.findAllByRole(roleId);
    }

    @PostMapping("/users/{userId}/roles")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRoleResponse assign(
            @PathVariable UUID userId,
            @Valid @RequestBody UserRoleAssignRequest request
    ) {
        return userRoleService.assign(userId, request);
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        userRoleService.revoke(userId, roleId);
    }
}