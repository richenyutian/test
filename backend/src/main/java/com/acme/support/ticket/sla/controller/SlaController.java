package com.acme.support.ticket.sla.controller;

import com.acme.support.ticket.audit.annotation.AuditLog;
import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.sla.dto.SlaRequests;
import com.acme.support.ticket.sla.dto.SlaResponses;
import com.acme.support.ticket.sla.service.SlaRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SLA 管理接口。
 */
@Tag(name = "SLA 管理")
@RestController
@RequestMapping("/v1/sla")
public class SlaController {

    private final SlaRuleService slaRuleService;

    public SlaController(SlaRuleService slaRuleService) {
        this.slaRuleService = slaRuleService;
    }

    @Operation(summary = "获取 SLA 规则与预警概览")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ticket:sla:view')")
    public ApiResponse<SlaResponses.SlaOverviewResponse> getOverview() {
        return ApiResponse.success(slaRuleService.getOverview());
    }

    @Operation(summary = "新增 SLA 规则")
    @PostMapping("/rules")
    @AuditLog(module = "SLA 管理", operation = "新增SLA规则")
    @PreAuthorize("hasAuthority('ticket:sla:update')")
    public ApiResponse<Long> createRule(@Valid @RequestBody SlaRequests.SlaRuleSaveRequest request) {
        return ApiResponse.success("新增成功", slaRuleService.createRule(request));
    }

    @Operation(summary = "修改 SLA 规则")
    @PutMapping("/rules/{slaRuleId}")
    @AuditLog(module = "SLA 管理", operation = "修改SLA规则")
    @PreAuthorize("hasAuthority('ticket:sla:update')")
    public ApiResponse<Void> updateRule(
            @PathVariable Long slaRuleId,
            @Valid @RequestBody SlaRequests.SlaRuleSaveRequest request
    ) {
        slaRuleService.updateRule(slaRuleId, request);
        return ApiResponse.success("修改成功", null);
    }

    @Operation(summary = "手动执行 SLA 超时扫描")
    @PostMapping("/scan")
    @PreAuthorize("hasAuthority('ticket:sla:view')")
    public ApiResponse<SlaResponses.SlaScanResultResponse> scanTimeouts() {
        return ApiResponse.success("扫描完成", slaRuleService.scanTimeouts());
    }
}
