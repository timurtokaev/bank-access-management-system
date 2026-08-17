package com.timurtokaev.bankaccess.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Repository
public class AuditLogWriter {

    private static final String INSERT_SQL = """
            INSERT INTO audit_logs (
                id,
                actor_user_id,
                actor_username,
                action,
                entity_type,
                entity_id,
                result,
                ip_address,
                details
            )
            VALUES (
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                ?,
                CAST(? AS inet),
                CAST(? AS jsonb)
            )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AuditLogWriter(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void write(
            UUID actorUserId,
            String actorUsername,
            String action,
            String entityType,
            UUID entityId,
            String result,
            Map<String, Object> details
    ) {
        String detailsJson = serializeDetails(
                details
        );

        jdbcTemplate.update(
                INSERT_SQL,
                UUID.randomUUID(),
                actorUserId,
                actorUsername,
                action,
                entityType,
                entityId,
                result,
                currentIpAddress(),
                detailsJson
        );
    }

    private String serializeDetails(
            Map<String, Object> details
    ) {
        Map<String, Object> safeDetails =
                details == null
                        ? Map.of()
                        : details;

        try {
            return objectMapper.writeValueAsString(
                    safeDetails
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to serialize audit log details",
                    exception
            );
        }
    }

    private String currentIpAddress() {
        if (!(RequestContextHolder.getRequestAttributes()
                instanceof ServletRequestAttributes attributes)) {
            return null;
        }

        String remoteAddress =
                attributes.getRequest().getRemoteAddr();

        if (remoteAddress == null
                || remoteAddress.isBlank()) {
            return null;
        }

        return remoteAddress;
    }
}