package com.timurtokaev.bankaccess.config;

import com.timurtokaev.bankaccess.department.DepartmentController;
import com.timurtokaev.bankaccess.department.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void shouldReturnUnauthorizedWithoutJwt()
            throws Exception {
        mockMvc.perform(
                        get("/api/departments")
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(departmentService);
    }

    @Test
    void shouldReturnForbiddenWithoutRequiredPermission()
            throws Exception {
        mockMvc.perform(
                        get("/api/departments")
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

        verifyNoInteractions(departmentService);
    }

    @Test
    void shouldAllowAccessWithRequiredPermission()
            throws Exception {
        when(
                departmentService.findAllActive()
        ).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/departments")
                                .with(
                                        jwt().authorities(
                                                new SimpleGrantedAuthority(
                                                        "DEPARTMENT_VIEW"
                                                )
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().json("[]")
                );

        verify(departmentService)
                .findAllActive();
    }
}