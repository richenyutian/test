package com.acme.support.ticket.sla.dto;

import java.util.List;

public final class SlaResponses {

    private SlaResponses() {
    }

    public record SlaRuleResponse(
            Long slaRuleId,
            String priorityCode,
            int responseLimitMinutes,
            int resolveLimitMinutes,
            int enabledFlag,
            String remark
    ) {
    }

    public record SlaAlertResponse(
            Long ticketId,
            String ticketNo,
            String title,
            String alertType,
            String ownerName,
            String teamName,
            String remainingTime
    ) {
    }

    public record SlaOverviewResponse(
            List<SlaRuleResponse> rules,
            List<SlaAlertResponse> alerts
    ) {
    }

    public record SlaScanResultResponse(
            int responseTimeoutCount,
            int resolveTimeoutCount,
            int noticeCount
    ) {
    }
}
