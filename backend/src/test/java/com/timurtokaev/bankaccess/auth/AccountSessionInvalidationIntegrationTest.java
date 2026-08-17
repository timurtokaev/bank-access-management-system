package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.department.Department;
import com.timurtokaev.bankaccess.department.DepartmentRepository;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import com.timurtokaev.bankaccess.user.UserService;
import com.timurtokaev.bankaccess.user.UserStatus;
import com.timurtokaev.bankaccess.user.dto.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class AccountSessionInvalidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AccessTokenService accessTokenService;

    @Test
    void shouldRejectOldBearerTokenAfterDeactivateAndReactivate()
            throws Exception {
        Department department = departmentRepository.saveAndFlush(
                new Department(
                        "AUTH_VERSION_IT",
                        "Authentication Version Integration Test",
                        null
                )
        );

        User user = userRepository.saveAndFlush(
                new User(
                        "AUTH_VERSION_IT",
                        "auth_version_it",
                        "auth-version-it@example.com",
                        "not-used-password-hash",
                        "Auth",
                        "Version",
                        department
                )
        );

        List<String> permissions = List.of(
                "USER_UPDATE",
                "USER_VIEW"
        );

        String oldAccessToken = accessTokenService.issue(
                user.getId(),
                user.getUsername(),
                permissions,
                user.getAuthVersion()
        ).token();

        mockMvc.perform(
                        get("/api/users/{id}", user.getId())
                                .header(
                                        "Authorization",
                                        bearer(oldAccessToken)
                                )
                )
                .andExpect(status().isOk());

        userService.deactivate(user.getId());

        mockMvc.perform(
                        put("/api/users/{id}", user.getId())
                                .header(
                                        "Authorization",
                                        bearer(oldAccessToken)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson(
                                        department,
                                        UserStatus.ACTIVE
                                ))
                )
                .andExpect(status().isUnauthorized());

        userService.update(
                user.getId(),
                updateRequest(
                        department,
                        UserStatus.ACTIVE
                )
        );

        mockMvc.perform(
                        get("/api/users/{id}", user.getId())
                                .header(
                                        "Authorization",
                                        bearer(oldAccessToken)
                                )
                )
                .andExpect(status().isUnauthorized());

        User reactivatedUser = userRepository.findById(
                user.getId()
        ).orElseThrow();

        String newAccessToken = accessTokenService.issue(
                reactivatedUser.getId(),
                reactivatedUser.getUsername(),
                permissions,
                reactivatedUser.getAuthVersion()
        ).token();

        mockMvc.perform(
                        get(
                                "/api/users/{id}",
                                reactivatedUser.getId()
                        ).header(
                                "Authorization",
                                bearer(newAccessToken)
                        )
                )
                .andExpect(status().isOk());
    }

    private UserUpdateRequest updateRequest(
            Department department,
            UserStatus status
    ) {
        return new UserUpdateRequest(
                "AUTH_VERSION_IT",
                "auth_version_it",
                "auth-version-it@example.com",
                "Auth",
                "Version",
                department.getId(),
                status
        );
    }

    private String updateJson(
            Department department,
            UserStatus status
    ) {
        return """
                {
                  "employeeNumber": "AUTH_VERSION_IT",
                  "username": "auth_version_it",
                  "email": "auth-version-it@example.com",
                  "firstName": "Auth",
                  "lastName": "Version",
                  "departmentId": "%s",
                  "status": "%s"
                }
                """.formatted(
                department.getId(),
                status
        );
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }
}
