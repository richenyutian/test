package com.acme.support.ticket.system.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统管理模块响应对象集合。
 */
public final class SystemManageResponses {

    private SystemManageResponses() {
    }

    public record UserPageItemResponse(
            Long userId,
            String username,
            String displayName,
            String userType,
            Integer externalUserFlag,
            String phone,
            String email,
            String status,
            String remark,
            List<String> roles,
            String primaryHandleGroupName,
            LocalDateTime createdAt
    ) {
    }

    public record RoleResponse(
            Long roleId,
            String roleCode,
            String roleName,
            String dataScope,
            String status,
            String remark,
            List<Long> menuIds
    ) {
    }

    public record MenuResponse(
            Long menuId,
            Long parentId,
            String menuName,
            String menuType,
            String routePath,
            String componentPath,
            String permissionCode,
            String icon,
            Integer sortOrder,
            Integer visibleFlag,
            Integer enabledFlag,
            Integer keepAliveFlag,
            String remark,
            List<MenuResponse> children
    ) {
    }

    public record HandleGroupResponse(
            Long handleGroupId,
            String groupCode,
            String groupName,
            String groupType,
            Long leaderUserId,
            String leaderName,
            String status,
            String remark
    ) {
    }

    public record OptionResponse(
            Long value,
            String label
    ) {
    }
}
