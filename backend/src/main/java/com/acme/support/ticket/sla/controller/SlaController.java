package com.acme.support.ticket.sla.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.mock.MockTicketDataService;
import com.acme.support.ticket.sla.dto.SlaResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SLA 管理接口。
 */
@Tag(name = "SLA 管理")
@RestController
@RequestMapping("/v1/sla")
public class SlaController {

    private final MockTicketDataService mockTicketDataService;

    public SlaController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取 SLA 规则与预警概览")
    @GetMapping("/overview")
    public ApiResponse<SlaResponses.SlaOverviewResponse> getOverview() {
        return ApiResponse.success(mockTicketDataService.getSlaOverview());
    }
}
