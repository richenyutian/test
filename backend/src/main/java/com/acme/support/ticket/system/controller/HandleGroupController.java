package com.acme.support.ticket.system.controller;

import com.acme.support.ticket.audit.annotation.AuditLog;
import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.service.HandleGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 处理组管理控制器。
 */
@Tag(name = "系统管理-处理组")
@RestController
@RequestMapping("/v1/system/handle-groups")
public class HandleGroupController {

    private final HandleGroupService handleGroupService;

    public HandleGroupController(HandleGroupService handleGroupService) {
        this.handleGroupService = handleGroupService;
    }

    @Operation(summary = "处理组列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:group:view')")
    public ApiResponse<List<SystemManageResponses.HandleGroupResponse>> listGroups() {
        return ApiResponse.success(handleGroupService.listGroups());
    }

    @Operation(summary = "新增处理组")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:group:create')")
    @AuditLog(module = "系统管理", operation = "新增处理组")
    public ApiResponse<Long> createGroup(@Valid @RequestBody SystemManageRequests.HandleGroupSaveRequest request) {
        return ApiResponse.success("新增成功", handleGroupService.createGroup(request));
    }

    @Operation(summary = "修改处理组")
    @PutMapping("/{handleGroupId}")
    @PreAuthorize("hasAuthority('sys:group:update')")
    @AuditLog(module = "系统管理", operation = "修改处理组")
    public ApiResponse<Void> updateGroup(
            @PathVariable Long handleGroupId,
            @Valid @RequestBody SystemManageRequests.HandleGroupSaveRequest request
    ) {
        handleGroupService.updateGroup(handleGroupId, request);
        return ApiResponse.success("修改成功", null);
    }

    @Operation(summary = "处理组下拉选项")
    @GetMapping("/options")
    public ApiResponse<List<SystemManageResponses.OptionResponse>> listGroupOptions() {
        return ApiResponse.success(handleGroupService.listGroupOptions());
    }
}
