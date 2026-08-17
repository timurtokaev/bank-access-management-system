package com.timurtokaev.bankaccess.audit;

import com.timurtokaev.bankaccess.audit.dto.AuditLogResponse;
import com.timurtokaev.bankaccess.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@Import(SecurityConfig.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void shouldReturnUnauthorizedWithoutJwt()
            throws Exception {
        mockMvc.perform(
                        get("/api/audit-logs")
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldReturnForbiddenWithoutAuditLogViewPermission()
            throws Exception {
        mockMvc.perform(
                        get("/api/audit-logs")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "USER_VIEW"
                                                )
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );

        verifyNoInteractions(auditLogService);
    }

    @Test
    void shouldReturnAuditLogsWithRequiredPermission()
            throws Exception {
        AuditLogResponse response =
                new AuditLogResponse(
                        UUID.fromString(
                                "00000000-0000-4000-8000-000000000001"
                        ),
                        UUID.fromString(
                                "00000000-0000-4000-8000-000000000002"
                        ),
                        "admin",
                        "LOGIN",
                        "USER",
                        UUID.fromString(
                                "00000000-0000-4000-8000-000000000002"
                        ),
                        "SUCCESS",
                        "127.0.0.1",
                        null,
                        OffsetDateTime.parse(
                                "2026-08-17T10:00:00Z"
                        )
                );

        when(
                auditLogService.findLatest()
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get("/api/audit-logs")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "AUDIT_LOG_VIEW"
                                                )
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$[0].actorUsername")
                                .value("admin")
                )
                .andExpect(
                        jsonPath("$[0].action")
                                .value("LOGIN")
                )
                .andExpect(
                        jsonPath("$[0].result")
                                .value("SUCCESS")
                );

        verify(auditLogService)
                .findLatest();
    }
}