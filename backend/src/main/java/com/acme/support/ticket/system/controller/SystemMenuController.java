package com.acme.support.ticket.system.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.service.SystemMenuService;
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
 * 菜单管理控制器。
 */
@Tag(name = "系统管理-菜单")
@RestController
@RequestMapping("/v1/system/menus")
public class SystemMenuController {

    private final SystemMenuService systemMenuService;

    public SystemMenuController(SystemMenuService systemMenuService) {
        this.systemMenuService = systemMenuService;
    }

    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:menu:view')")
    public ApiResponse<List<SystemManageResponses.MenuResponse>> listMenuTree() {
        return ApiResponse.success(systemMenuService.listMenuTree());
    }

    @Operation(summary = "菜单下拉选项")
    @GetMapping("/options")
    public ApiResponse<List<SystemManageResponses.OptionResponse>> listMenuOptions() {
        return ApiResponse.success(systemMenuService.listMenuOptions());
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:menu:create')")
    public ApiResponse<Long> createMenu(@Valid @RequestBody SystemManageRequests.MenuSaveRequest request) {
        return ApiResponse.success("新增成功", systemMenuService.createMenu(request));
    }

    @Operation(summary = "修改菜单")
    @PutMapping("/{menuId}")
    @PreAuthorize("hasAuthority('sys:menu:update')")
    public ApiResponse<Void> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody SystemManageRequests.MenuSaveRequest request
    ) {
        systemMenuService.updateMenu(menuId, request);
        return ApiResponse.success("修改成功", null);
    }
}
