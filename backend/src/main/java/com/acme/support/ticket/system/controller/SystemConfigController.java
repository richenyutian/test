package com.acme.support.ticket.system.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.mock.MockTicketDataService;
import com.acme.support.ticket.system.dto.SystemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置接口。
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/v1/system")
public class SystemConfigController {

    private final MockTicketDataService mockTicketDataService;

    public SystemConfigController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取系统配置概览")
    @GetMapping("/overview")
    public ApiResponse<SystemResponses.SystemConfigOverviewResponse> getOverview() {
        return ApiResponse.success(mockTicketDataService.getSystemConfigOverview());
    }
}
