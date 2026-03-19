package com.acme.support.ticket.system.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.common.api.PageResponse;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.service.SystemUserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理控制器。
 */
@Tag(name = "系统管理-用户")
@RestController
@RequestMapping("/v1/system/users")
public class SystemUserController {

    private final SystemUserService systemUserService;

    public SystemUserController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @Operation(summary = "用户分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:view')")
    public ApiResponse<PageResponse<SystemManageResponses.UserPageItemResponse>> pageUsers(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(systemUserService.pageUsers(current, size, keyword));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:create')")
    public ApiResponse<Long> createUser(@Valid @RequestBody SystemManageRequests.UserSaveRequest request) {
        return ApiResponse.success("新增成功", systemUserService.createUser(request));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('sys:user:update')")
    public ApiResponse<Void> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody SystemManageRequests.UserSaveRequest request
    ) {
        systemUserService.updateUser(userId, request);
        return ApiResponse.success("修改成功", null);
    }

    @Operation(summary = "用户下拉选项")
    @GetMapping("/options")
    public ApiResponse<List<SystemManageResponses.OptionResponse>> listUserOptions() {
        return ApiResponse.success(systemUserService.listUserOptions());
    }
}
