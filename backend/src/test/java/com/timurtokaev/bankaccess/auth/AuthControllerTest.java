package com.timurtokaev.bankaccess.auth;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import com.timurtokaev.bankaccess.auth.dto.AuthTokenResponse;
import com.timurtokaev.bankaccess.common.error.UnauthorizedException;
import com.timurtokaev.bankaccess.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String VALID_REFRESH_TOKEN =
            "A".repeat(43);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void shouldAllowLoginWithoutCsrfToken()
            throws Exception {
        AuthTokenResponse response =
                new AuthTokenResponse(
                        "Bearer",
                        "access-token",
                        OffsetDateTime.parse(
                                "2026-08-16T12:10:00Z"
                        ),
                        "refresh-token",
                        OffsetDateTime.parse(
                                "2026-09-15T12:00:00Z"
                        )
                );

        when(
                authService.login(any())
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "correct-password"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.accessToken")
                                .value("access-token")
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .value("refresh-token")
                );
    }

    @Test
    void shouldRejectInvalidLoginRequest()
            throws Exception {
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "",
                                          "password": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.fieldErrors.username")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.fieldErrors.password")
                                .exists()
                );

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {
        when(
                authService.login(any())
        ).thenThrow(new UnauthorizedException());

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "username": "admin",
                                          "password": "wrong-password"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Authentication failed")
                );
    }

    @Test
    void shouldAllowRefreshWithoutCsrfToken()
            throws Exception {
        AuthTokenResponse response =
                new AuthTokenResponse(
                        "Bearer",
                        "new-access-token",
                        OffsetDateTime.parse(
                                "2026-08-16T12:20:00Z"
                        ),
                        "new-refresh-token",
                        OffsetDateTime.parse(
                                "2026-09-15T12:10:00Z"
                        )
                );

        when(
                authService.refresh(any())
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "refreshToken": "%s"
                                        }
                                        """.formatted(
                                        VALID_REFRESH_TOKEN
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.accessToken")
                                .value("new-access-token")
                )
                .andExpect(
                        jsonPath("$.refreshToken")
                                .value("new-refresh-token")
                );
    }

    @Test
    void shouldRejectInvalidRefreshRequest()
            throws Exception {
        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "refreshToken": "invalid-token"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.fieldErrors.refreshToken")
                                .exists()
                );

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnUnauthorizedForInvalidRefreshToken()
            throws Exception {
        when(
                authService.refresh(any())
        ).thenThrow(new UnauthorizedException());

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "refreshToken": "%s"
                                        }
                                        """.formatted(
                                        VALID_REFRESH_TOKEN
                                ))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Authentication failed")
                );
    }
    @Test
    void shouldRequireAuthenticationForBusinessApi()
            throws Exception {
        mockMvc.perform(
                        get("/api/departments")
                )
                .andExpect(
                        status().isUnauthorized()
                );

        verifyNoInteractions(authService);
    }
}