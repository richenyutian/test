package com.acme.support.ticket.ticket.controller;

import com.acme.support.ticket.audit.annotation.AuditLog;
import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.common.api.PageResponse;
import com.acme.support.ticket.ticket.dto.TicketRequests;
import com.acme.support.ticket.ticket.dto.TicketResponses;
import com.acme.support.ticket.ticket.entity.TicketAttachmentEntity;
import com.acme.support.ticket.ticket.query.TicketQuery;
import com.acme.support.ticket.ticket.service.FileStorageService;
import com.acme.support.ticket.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 工单管理接口，覆盖工单列表、详情、流转规则说明。
 */
@Tag(name = "工单管理")
@RestController
@RequestMapping("/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final FileStorageService fileStorageService;

    public TicketController(TicketService ticketService, FileStorageService fileStorageService) {
        this.ticketService = ticketService;
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "获取工单分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('ticket:view')")
    public ApiResponse<PageResponse<TicketResponses.TicketListItemResponse>> pageTickets(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String currentStatus,
            @RequestParam(required = false) String priorityCode,
            @RequestParam(required = false) Long handleGroupId,
            @RequestParam(required = false) Long handlerUserId
    ) {
        return ApiResponse.success(ticketService.pageTickets(
                current,
                size,
                new TicketQuery(keyword, currentStatus, priorityCode, handleGroupId, handlerUserId)
        ));
    }

    @Operation(summary = "创建工单")
    @PostMapping
    @AuditLog(module = "工单管理", operation = "创建工单")
    @PreAuthorize("hasAuthority('ticket:create')")
    public ApiResponse<Long> createTicket(@Valid @RequestBody TicketRequests.TicketCreateRequest request) {
        return ApiResponse.success("创建成功", ticketService.createTicket(request));
    }

    @Operation(summary = "获取工单详情")
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAuthority('ticket:view')")
    public ApiResponse<TicketResponses.TicketDetailResponse> getTicketDetail(@PathVariable Long ticketId) {
        return ApiResponse.success(ticketService.getTicketDetail(ticketId));
    }

    @Operation(summary = "受理工单")
    @PutMapping("/{ticketId}/accept")
    @AuditLog(module = "工单管理", operation = "受理工单")
    @PreAuthorize("hasAuthority('ticket:accept')")
    public ApiResponse<Void> acceptTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketAcceptRequest request
    ) {
        ticketService.acceptTicket(ticketId, request);
        return ApiResponse.success("受理成功", null);
    }

    @Operation(summary = "分派工单")
    @PutMapping("/{ticketId}/assign")
    @AuditLog(module = "工单管理", operation = "分派工单")
    @PreAuthorize("hasAuthority('ticket:assign')")
    public ApiResponse<Void> assignTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketAssignRequest request
    ) {
        ticketService.assignTicket(ticketId, request);
        return ApiResponse.success("分派成功", null);
    }

    @Operation(summary = "接单处理")
    @PutMapping("/{ticketId}/claim")
    @AuditLog(module = "工单管理", operation = "接单处理")
    @PreAuthorize("hasAuthority('ticket:process')")
    public ApiResponse<Void> claimTicket(@PathVariable Long ticketId) {
        ticketService.claimTicket(ticketId);
        return ApiResponse.success("接单成功", null);
    }

    @Operation(summary = "转派工单")
    @PutMapping("/{ticketId}/transfer")
    @AuditLog(module = "工单管理", operation = "转派工单")
    @PreAuthorize("hasAuthority('ticket:transfer')")
    public ApiResponse<Void> transferTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketTransferRequest request
    ) {
        ticketService.transferTicket(ticketId, request);
        return ApiResponse.success("转派成功", null);
    }

    @Operation(summary = "挂起工单")
    @PutMapping("/{ticketId}/suspend")
    @AuditLog(module = "工单管理", operation = "挂起工单")
    @PreAuthorize("hasAuthority('ticket:suspend')")
    public ApiResponse<Void> suspendTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketSuspendRequest request
    ) {
        ticketService.suspendTicket(ticketId, request);
        return ApiResponse.success("挂起成功", null);
    }

    @Operation(summary = "恢复工单")
    @PutMapping("/{ticketId}/resume")
    @AuditLog(module = "工单管理", operation = "恢复工单")
    @PreAuthorize("hasAuthority('ticket:suspend')")
    public ApiResponse<Void> resumeTicket(@PathVariable Long ticketId) {
        ticketService.resumeTicket(ticketId);
        return ApiResponse.success("恢复成功", null);
    }

    @Operation(summary = "完成工单处理")
    @PutMapping("/{ticketId}/complete")
    @AuditLog(module = "工单管理", operation = "完成工单")
    @PreAuthorize("hasAuthority('ticket:finish')")
    public ApiResponse<Void> completeTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketCompleteRequest request
    ) {
        ticketService.completeTicket(ticketId, request);
        return ApiResponse.success("处理完成，等待用户确认", null);
    }

    @Operation(summary = "确认工单解决")
    @PutMapping("/{ticketId}/confirm")
    @AuditLog(module = "工单管理", operation = "确认工单")
    @PreAuthorize("hasAnyAuthority('ticket:reopen','ticket:create')")
    public ApiResponse<Void> confirmTicket(@PathVariable Long ticketId) {
        ticketService.confirmTicket(ticketId);
        return ApiResponse.success("确认成功", null);
    }

    @Operation(summary = "关闭工单")
    @PutMapping("/{ticketId}/close")
    @AuditLog(module = "工单管理", operation = "关闭工单")
    @PreAuthorize("hasAuthority('ticket:close')")
    public ApiResponse<Void> closeTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketCloseRequest request
    ) {
        ticketService.closeTicket(ticketId, request);
        return ApiResponse.success("关闭成功", null);
    }

    @Operation(summary = "重开工单")
    @PutMapping("/{ticketId}/reopen")
    @AuditLog(module = "工单管理", operation = "重开工单")
    @PreAuthorize("hasAuthority('ticket:reopen')")
    public ApiResponse<Void> reopenTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketReopenRequest request
    ) {
        ticketService.reopenTicket(ticketId, request);
        return ApiResponse.success("重开成功", null);
    }

    @Operation(summary = "升级工单")
    @PutMapping("/{ticketId}/escalate")
    @AuditLog(module = "工单管理", operation = "升级工单")
    @PreAuthorize("hasAuthority('ticket:escalate')")
    public ApiResponse<Void> escalateTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketRequests.TicketEscalateRequest request
    ) {
        ticketService.escalateTicket(ticketId, request);
        return ApiResponse.success("升级成功", null);
    }

    @Operation(summary = "上传工单附件")
    @PostMapping(path = "/{ticketId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AuditLog(module = "工单管理", operation = "上传工单附件")
    @PreAuthorize("hasAuthority('ticket:view')")
    public ApiResponse<TicketResponses.TicketAttachmentResponse> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.success("上传成功", ticketService.uploadAttachment(ticketId, file));
    }

    @Operation(summary = "下载工单附件")
    @GetMapping("/attachments/{attachmentId}/download")
    @PreAuthorize("hasAuthority('ticket:view')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        TicketAttachmentEntity attachment = ticketService.getAttachment(attachmentId);
        Resource resource = fileStorageService.loadAsResource(attachment.getStoragePath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(summary = "获取工单状态流转规则")
    @GetMapping("/lifecycle-rules")
    public ApiResponse<List<TicketResponses.TicketLifecycleRuleResponse>> getLifecycleRules() {
        return ApiResponse.success(ticketService.listLifecycleRules());
    }
}
