package com.acme.support.ticket.system.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.service.SystemRoleService;
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
 * 角色管理控制器。
 */
@Tag(name = "系统管理-角色")
@RestController
@RequestMapping("/v1/system/roles")
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    public SystemRoleController(SystemRoleService systemRoleService) {
        this.systemRoleService = systemRoleService;
    }

    @Operation(summary = "角色列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:role:view')")
    public ApiResponse<List<SystemManageResponses.RoleResponse>> listRoles() {
        return ApiResponse.success(systemRoleService.listRoles());
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:create')")
    public ApiResponse<Long> createRole(@Valid @RequestBody SystemManageRequests.RoleSaveRequest request) {
        return ApiResponse.success("新增成功", systemRoleService.createRole(request));
    }

    @Operation(summary = "修改角色")
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('sys:role:update')")
    public ApiResponse<Void> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody SystemManageRequests.RoleSaveRequest request
    ) {
        systemRoleService.updateRole(roleId, request);
        return ApiResponse.success("修改成功", null);
    }

    @Operation(summary = "角色下拉选项")
    @GetMapping("/options")
    public ApiResponse<List<SystemManageResponses.OptionResponse>> listRoleOptions() {
        return ApiResponse.success(systemRoleService.listRoleOptions());
    }
}
