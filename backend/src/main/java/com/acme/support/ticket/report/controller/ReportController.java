package com.acme.support.ticket.report.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.mock.MockTicketDataService;
import com.acme.support.ticket.report.dto.ReportResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报表统计接口。
 */
@Tag(name = "报表统计")
@RestController
@RequestMapping("/v1/reports")
public class ReportController {

    private final MockTicketDataService mockTicketDataService;

    public ReportController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取统计报表概览")
    @GetMapping("/overview")
    public ApiResponse<ReportResponses.ReportOverviewResponse> getOverview() {
        return ApiResponse.success(mockTicketDataService.getReportOverview());
    }
}
