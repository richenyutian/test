package com.acme.support.ticket.ticket.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class TicketResponses {

    private TicketResponses() {
    }

    public record TicketListItemResponse(
            String ticketId,
            String ticketNo,
            String title,
            String requesterName,
            String source,
            String ticketType,
            String categoryName,
            String priority,
            String urgencyLevel,
            String status,
            String currentAssigneeName,
            String currentGroupName,
            boolean escalated,
            boolean timeout,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record TicketFlowRecordResponse(
            String actionType,
            String operatorName,
            String fromStatus,
            String toStatus,
            String remark,
            LocalDateTime operatedAt
    ) {
    }

    public record TicketCommentResponse(
            String commentId,
            String authorName,
            String authorRole,
            String content,
            LocalDateTime createdAt
    ) {
    }

    public record TicketDetailResponse(
            String ticketId,
            String ticketNo,
            String title,
            String description,
            String requesterName,
            String contactPhone,
            String customerName,
            String departmentName,
            String source,
            String ticketType,
            String categoryName,
            String priority,
            String urgencyLevel,
            String status,
            String currentAssigneeName,
            String currentGroupName,
            LocalDateTime responseDeadline,
            LocalDateTime resolveDeadline,
            LocalDateTime createdAt,
            LocalDateTime acceptedAt,
            LocalDateTime assignedAt,
            LocalDateTime completedAt,
            LocalDateTime closedAt,
            String satisfactionLevel,
            String satisfactionComment,
            List<String> tags,
            List<TicketFlowRecordResponse> flowRecords,
            List<TicketCommentResponse> comments
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
