package com.acme.support.ticket.audit.controller;

import com.acme.support.ticket.audit.dto.AuditResponses;
import com.acme.support.ticket.audit.service.AuditLogService;
import com.acme.support.ticket.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志接口。
 */
@Tag(name = "审计日志")
@RestController
@RequestMapping("/v1/audits")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "获取审计日志")
    @GetMapping
    @PreAuthorize("hasAuthority('audit:view')")
    public ApiResponse<AuditResponses.AuditLogPageResponse> getAuditLogs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.success(auditLogService.page(current, size));
    }
}
