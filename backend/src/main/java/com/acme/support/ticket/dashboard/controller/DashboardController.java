package com.acme.support.ticket.dashboard.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.dashboard.dto.DashboardResponses;
import com.acme.support.ticket.mock.MockTicketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台总览接口。
 */
@Tag(name = "工作台")
@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {

    private final MockTicketDataService mockTicketDataService;

    public DashboardController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取工作台总览数据")
    @GetMapping("/overview")
    public ApiResponse<DashboardResponses.WorkbenchOverviewResponse> getOverview() {
        return ApiResponse.success(mockTicketDataService.getWorkbenchOverview());
    }
}
