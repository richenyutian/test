package com.acme.support.ticket.ticket.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.common.api.PageResponse;
import com.acme.support.ticket.mock.MockTicketDataService;
import com.acme.support.ticket.ticket.dto.TicketResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工单管理接口，覆盖工单列表、详情、流转规则说明。
 */
@Tag(name = "工单管理")
@RestController
@RequestMapping("/v1/tickets")
public class TicketController {

    private final MockTicketDataService mockTicketDataService;

    public TicketController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取工单分页列表")
    @GetMapping
    public ApiResponse<PageResponse<TicketResponses.TicketListItemResponse>> pageTickets() {
        List<TicketResponses.TicketListItemResponse> tickets = mockTicketDataService.listTickets();
        return ApiResponse.success(new PageResponse<>(1, 10, tickets.size(), tickets.subList(0, Math.min(10, tickets.size()))));
    }

    @Operation(summary = "获取工单详情")
    @GetMapping("/{ticketId}")
    public ApiResponse<TicketResponses.TicketDetailResponse> getTicketDetail(@PathVariable String ticketId) {
        return ApiResponse.success(mockTicketDataService.getTicketDetail(ticketId));
    }

    @Operation(summary = "获取工单状态流转规则")
    @GetMapping("/lifecycle-rules")
    public ApiResponse<List<TicketResponses.TicketLifecycleRuleResponse>> getLifecycleRules() {
        return ApiResponse.success(mockTicketDataService.getLifecycleRules());
    }
}
