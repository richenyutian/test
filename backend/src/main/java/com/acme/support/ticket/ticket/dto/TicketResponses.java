package com.acme.support.ticket.ticket.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class TicketResponses {

    private TicketResponses() {
    }

    public record TicketListItemResponse(
            Long ticketId,
            String ticketNo,
            String title,
            String requesterName,
            String sourceCode,
            String ticketTypeCode,
            String categoryCode,
            String priorityCode,
            String currentStatus,
            Long currentHandlerUserId,
            String currentHandlerName,
            Long currentHandleGroupId,
            String currentHandleGroupName,
            boolean escalatedFlag,
            boolean timeoutFlag,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record TicketFlowRecordResponse(
            Long flowRecordId,
            String actionCode,
            String operatorName,
            String fromStatus,
            String toStatus,
            String operateDescription,
            LocalDateTime operateTime
    ) {
    }

    public record TicketAttachmentResponse(
            Long attachmentId,
            String originalFileName,
            String storageFileName,
            String fileExtension,
            Long fileSize,
            String uploaderName,
            LocalDateTime createdAt
    ) {
    }

    public record TicketDetailResponse(
            Long ticketId,
            String ticketNo,
            String title,
            String description,
            Long requesterUserId,
            String requesterName,
            String contactPhone,
            Integer externalUserFlag,
            String sourceCode,
            String ticketTypeCode,
            String categoryCode,
            String priorityCode,
            String currentStatus,
            Long currentHandlerUserId,
            String currentHandlerName,
            Long currentHandleGroupId,
            String currentHandleGroupName,
            Integer escalatedFlag,
            String escalateReason,
            LocalDateTime escalateTime,
            Integer timeoutFlag,
            LocalDateTime responseDeadline,
            LocalDateTime resolveDeadline,
            LocalDateTime createdAt,
            LocalDateTime acceptedAt,
            LocalDateTime assignedAt,
            LocalDateTime processedAt,
            LocalDateTime userConfirmedAt,
            LocalDateTime closedAt,
            String suspendReason,
            String closeReason,
            String reopenReason,
            String resolutionSummary,
            List<TicketAttachmentResponse> attachments,
            List<TicketFlowRecordResponse> flowRecords,
            List<String> allowedActions
    ) {
    }

    public record TicketLifecycleRuleResponse(
            String currentStatus,
            String actionName,
            String targetStatus,
            List<String> allowedRoles
    ) {
    }
}
