package com.acme.support.ticket.sla.dto;

import java.util.List;

public final class SlaResponses {

    private SlaResponses() {
    }

    public record SlaRuleResponse(
            String ruleCode,
            String ruleName,
            String categoryName,
            int responseMinutes,
            int resolveMinutes,
            int responseWarningMinutes,
            int resolveWarningMinutes,
            boolean autoEscalate
    ) {
    }

    public record SlaAlertResponse(
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
}
