package com.timurtokaev.bankaccess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/departments/**",
                                "/api/users/**",
                                "/api/permissions/**",
                                "/api/roles/**"
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorize -> authorize

                        // Authentication endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).permitAll()

                        // User role assignments
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/*/roles",
                                "/api/roles/*/users"
                        ).hasAuthority("ROLE_VIEW")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users/*/roles"
                        ).hasAuthority("ROLE_ASSIGN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/*/roles/*"
                        ).hasAuthority("ROLE_REVOKE")

                        // Effective permissions and role permissions
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/*/effective-permissions",
                                "/api/roles/*/permissions",
                                "/api/permissions/*/roles"
                        ).hasAuthority("PERMISSION_VIEW")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/roles/*/permissions"
                        ).hasAuthority("PERMISSION_GRANT")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/roles/*/permissions/*"
                        ).hasAuthority("PERMISSION_REVOKE")

                        // Departments
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/departments/**"
                        ).hasAuthority("DEPARTMENT_VIEW")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/departments"
                        ).hasAuthority("DEPARTMENT_CREATE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/departments/*"
                        ).hasAuthority("DEPARTMENT_UPDATE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/departments/*"
                        ).hasAuthority("DEPARTMENT_DEACTIVATE")

                        // Users
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/**"
                        ).hasAuthority("USER_VIEW")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/users"
                        ).hasAuthority("USER_CREATE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/users/*"
                        ).hasAuthority("USER_UPDATE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/*"
                        ).hasAuthority("USER_DEACTIVATE")

                        // Roles
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/roles/**"
                        ).hasAuthority("ROLE_VIEW")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/roles"
                        ).hasAuthority("ROLE_CREATE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/roles/**"
                        ).hasAuthority("ROLE_UPDATE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/roles/*"
                        ).hasAuthority("ROLE_DEACTIVATE")

                        // Permissions
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/permissions/**"
                        ).hasAuthority("PERMISSION_VIEW")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/permissions"
                        ).hasAuthority("PERMISSION_CREATE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/permissions/**"
                        ).hasAuthority("PERMISSION_UPDATE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/permissions/*"
                        ).hasAuthority("PERMISSION_DEACTIVATE")

                        // Unknown API endpoints are denied by default
                        .requestMatchers(
                                "/api/**"
                        ).denyAll()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}