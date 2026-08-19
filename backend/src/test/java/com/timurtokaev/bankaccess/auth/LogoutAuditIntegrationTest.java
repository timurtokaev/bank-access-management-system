package com.timurtokaev.bankaccess.auth;

import com.timurtokaev.bankaccess.department.Department;
import com.timurtokaev.bankaccess.department.DepartmentRepository;
import com.timurtokaev.bankaccess.user.User;
import com.timurtokaev.bankaccess.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class LogoutAuditIntegrationTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TransactionalLogoutService transactionalLogoutService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldWriteSingleLogoutAuditWhenRefreshTokenIsRevoked() {
        Department department =
                departmentRepository.saveAndFlush(
                        new Department(
                                "LOGOUT_AUDIT_IT",
                                "Logout Audit Integration Test",
                                null
                        )
                );

        User user = userRepository.saveAndFlush(
                new User(
                        "LOGOUT_AUDIT_IT",
                        "logout_audit_it",
                        "logout-audit-it@example.com",
                        "not-used-password-hash",
                        "Logout",
                        "Audit",
                        department
                )
        );

        IssuedRefreshToken refreshToken =
                refreshTokenService.issue(user);

        transactionalLogoutService.execute(
                refreshToken.token()
        );

        // Repeated logout must remain idempotent
        // and must not create another SUCCESS event.
        transactionalLogoutService.execute(
                refreshToken.token()
        );

        Integer auditCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM audit_logs
                        WHERE action = 'LOGOUT'
                          AND actor_user_id = ?
                        """,
                        Integer.class,
                        user.getId()
                );

        assertEquals(
                1,
                auditCount
        );

        Map<String, Object> audit =
                jdbcTemplate.queryForMap(
                        """
                        SELECT
                            actor_user_id::text
                                AS actor_user_id,
                            actor_username,
                            entity_type,
                            entity_id::text
                                AS entity_id,
                            result,
                            details ->> 'authenticationMethod'
                                AS authentication_method
                        FROM audit_logs
                        WHERE action = 'LOGOUT'
                          AND actor_user_id = ?
                        ORDER BY occurred_at DESC
                        LIMIT 1
                        """,
                        user.getId()
                );

        assertEquals(
                user.getId().toString(),
                audit.get("actor_user_id")
        );

        assertEquals(
                "logout_audit_it",
                audit.get("actor_username")
        );

        assertEquals(
                "USER",
                audit.get("entity_type")
        );

        assertEquals(
                user.getId().toString(),
                audit.get("entity_id")
        );

        assertEquals(
                "SUCCESS",
                audit.get("result")
        );

        assertEquals(
                "REFRESH_TOKEN",
                audit.get("authentication_method")
        );
    }
}