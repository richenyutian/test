package com.acme.support.ticket.audit.controller;

import com.acme.support.ticket.audit.dto.AuditResponses;
import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.mock.MockTicketDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志接口。
 */
@Tag(name = "审计日志")
@RestController
@RequestMapping("/v1/audits")
public class AuditLogController {

    private final MockTicketDataService mockTicketDataService;

    public AuditLogController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取审计日志")
    @GetMapping
    public ApiResponse<AuditResponses.AuditLogPageResponse> getAuditLogs() {
        return ApiResponse.success(mockTicketDataService.getAuditLogPage());
    }
}
