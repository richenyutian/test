package com.acme.support.ticket.report.dto;

import java.util.List;

public final class ReportResponses {

    private ReportResponses() {
    }

    public record MetricItem(String name, long value, String extra) {
    }

    public record ReportOverviewResponse(
            List<MetricItem> ticketTrend,
            List<MetricItem> statusStatistics,
            List<MetricItem> priorityStatistics,
            List<MetricItem> handleGroupStatistics,
            List<MetricItem> assigneeStatistics,
            List<MetricItem> slaStatistics,
            List<MetricItem> timeoutStatistics
    ) {
    }
}
