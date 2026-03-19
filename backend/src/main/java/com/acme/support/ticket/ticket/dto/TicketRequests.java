package com.acme.support.ticket.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 工单模块请求对象集合。
 */
public final class TicketRequests {

    private TicketRequests() {
    }

    public record TicketCreateRequest(
            @NotBlank String title,
            @NotBlank String description,
            @NotBlank String contactPhone,
            @NotNull Integer externalUserFlag,
            @NotBlank String sourceCode,
            @NotBlank String ticketTypeCode,
            @NotBlank String categoryCode,
            @NotBlank String priorityCode
    ) {
    }

    public record TicketAcceptRequest(
            @NotBlank String priorityCode,
            @NotBlank String categoryCode
    ) {
    }

    public record TicketAssignRequest(
            @NotNull Long handleGroupId,
            Long handlerUserId,
            String operateDescription
    ) {
    }

    public record TicketTransferRequest(
            @NotNull Long handleGroupId,
            Long handlerUserId,
            @NotBlank String operateDescription
    ) {
    }

    public record TicketSuspendRequest(
            @NotBlank String suspendReason
    ) {
    }

    public record TicketCompleteRequest(
            @NotBlank String resolutionSummary
    ) {
    }

    public record TicketCloseRequest(
            @NotBlank String closeReason
    ) {
    }

    public record TicketReopenRequest(
            @NotBlank String reopenReason
    ) {
    }

    public record TicketEscalateRequest(
            @NotBlank String escalateReason
    ) {
    }
}
