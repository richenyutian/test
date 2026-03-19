package com.acme.support.ticket.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 系统管理模块请求对象集合。
 */
public final class SystemManageRequests {

    private SystemManageRequests() {
    }

    public record UserSaveRequest(
            @NotBlank String username,
            @NotBlank String displayName,
            @NotBlank String userType,
            @NotNull Integer externalUserFlag,
            String phone,
            String email,
            @NotBlank String status,
            String remark,
            @NotEmpty List<Long> roleIds,
            Long primaryHandleGroupId
    ) {
    }

    public record RoleSaveRequest(
            @NotBlank String roleCode,
            @NotBlank String roleName,
            @NotBlank String dataScope,
            @NotBlank String status,
            String remark,
            @NotEmpty List<Long> menuIds
    ) {
    }

    public record MenuSaveRequest(
            @NotNull Long parentId,
            @NotBlank String menuName,
            @NotBlank String menuType,
            String routePath,
            String componentPath,
            String permissionCode,
            String icon,
            @NotNull Integer sortOrder,
            @NotNull Integer visibleFlag,
            @NotNull Integer enabledFlag,
            @NotNull Integer keepAliveFlag,
            String remark
    ) {
    }

    public record HandleGroupSaveRequest(
            @NotBlank String groupCode,
            @NotBlank String groupName,
            @NotBlank String groupType,
            Long leaderUserId,
            String leaderName,
            @NotBlank String status,
            String remark
    ) {
    }
}
