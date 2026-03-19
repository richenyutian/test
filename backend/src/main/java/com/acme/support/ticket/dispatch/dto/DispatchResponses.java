package com.acme.support.ticket.dispatch.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class DispatchResponses {

    private DispatchResponses() {
    }

    public record DispatchTicketResponse(
            String ticketNo,
            String title,
            String categoryName,
            String priority,
            String recommendedTeam,
            String recommendedAssignee,
            LocalDateTime createdAt
    ) {
    }

    public record DispatchBoardResponse(
            long pendingDispatchCount,
            long escalatedCount,
            List<DispatchTicketResponse> pendingDispatchTickets
    ) {
    }
}
