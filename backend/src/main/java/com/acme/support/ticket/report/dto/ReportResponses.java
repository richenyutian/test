package com.acme.support.ticket.report.dto;

import java.util.List;

public final class ReportResponses {

    private ReportResponses() {
    }

    public record MetricItem(String name, long count, String extra) {
    }

    public record ReportOverviewResponse(
            List<MetricItem> ticketTrend,
            List<MetricItem> statusStatistics,
            List<MetricItem> categoryStatistics,
            List<MetricItem> assigneeStatistics,
            List<MetricItem> departmentStatistics,
            List<MetricItem> slaStatistics,
            List<MetricItem> satisfactionStatistics
    ) {
    }
}
