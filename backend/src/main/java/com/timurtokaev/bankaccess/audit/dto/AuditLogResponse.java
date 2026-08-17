package com.timurtokaev.bankaccess.audit.dto;

import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(

        UUID id,

        UUID actorUserId,

        String actorUsername,

        String action,

        String entityType,

        UUID entityId,

        String result,

        String ipAddress,

        JsonNode details,

        OffsetDateTime occurredAt

) {
}