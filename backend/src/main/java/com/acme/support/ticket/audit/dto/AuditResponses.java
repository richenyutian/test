package com.acme.support.ticket.audit.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class AuditResponses {

    private AuditResponses() {
    }

    public record AuditLogResponse(
            String logId,
            String moduleName,
            String operationName,
            String operatorName,
            String requestPath,
            String result,
            LocalDateTime operatedAt
    ) {
    }

    public record AuditLogPageResponse(
            long total,
            List<AuditLogResponse> records
    ) {
    }
}
