package com.acme.support.ticket.dashboard.dto;

import java.util.List;

public final class DashboardResponses {

    private DashboardResponses() {
    }

    public record MetricCard(String label, String value, String trend) {
    }

    public record TrendPoint(String date, long count) {
    }

    public record WorkbenchOverviewResponse(
            List<MetricCard> metrics,
            List<TrendPoint> ticketTrend,
            List<MetricCard> statusDistribution,
            List<MetricCard> personalTasks
    ) {
    }
}
