package com.timurtokaev.bankaccess.audit;

import com.timurtokaev.bankaccess.audit.dto.AuditLogResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class AuditLogRepository {

    private static final String FIND_LATEST_SQL = """
            SELECT
                id,
                actor_user_id,
                actor_username,
                action,
                entity_type,
                entity_id,
                result,
                host(ip_address) AS ip_address,
                details::text AS details,
                occurred_at
            FROM audit_logs
            ORDER BY occurred_at DESC, id DESC
            LIMIT 100
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AuditLogRepository(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<AuditLogResponse> findLatest() {
        return jdbcTemplate.query(
                FIND_LATEST_SQL,
                this::mapRow
        );
    }

    private AuditLogResponse mapRow(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {
        return new AuditLogResponse(
                resultSet.getObject(
                        "id",
                        UUID.class
                ),
                resultSet.getObject(
                        "actor_user_id",
                        UUID.class
                ),
                resultSet.getString(
                        "actor_username"
                ),
                resultSet.getString(
                        "action"
                ),
                resultSet.getString(
                        "entity_type"
                ),
                resultSet.getObject(
                        "entity_id",
                        UUID.class
                ),
                resultSet.getString(
                        "result"
                ),
                resultSet.getString(
                        "ip_address"
                ),
                parseDetails(
                        resultSet.getString(
                                "details"
                        )
                ),
                resultSet.getObject(
                        "occurred_at",
                        OffsetDateTime.class
                )
        );
    }

    private JsonNode parseDetails(
            String details
    ) throws SQLException {
        try {
            return objectMapper.readTree(details);
        } catch (JacksonException exception) {
            throw new SQLException(
                    "Failed to parse audit log details",
                    exception
            );
        }
    }
}