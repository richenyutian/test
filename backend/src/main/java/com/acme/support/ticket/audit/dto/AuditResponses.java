package com.acme.support.ticket.audit.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class AuditResponses {

    private AuditResponses() {
    }

    public record AuditLogResponse(
            Long auditLogId,
            Long operatorUserId,
            String moduleName,
            String operationType,
            String operatorName,
            Long businessId,
            String operationDescription,
            String requestPath,
            String requestIp,
            LocalDateTime createdAt
    ) {
    }

    public record AuditLogPageResponse(
            long current,
            long size,
            long total,
            List<AuditLogResponse> records
    ) {
    }
}
