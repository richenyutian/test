package com.acme.support.ticket.system.dto;

import java.util.List;

public final class SystemResponses {

    private SystemResponses() {
    }

    public record DictionaryItemResponse(String code, String name, String description) {
    }

    public record RolePermissionResponse(
            String roleCode,
            String roleName,
            List<String> menuPermissions,
            List<String> buttonPermissions,
            String dataScope
    ) {
    }

    public record SystemConfigOverviewResponse(
            List<DictionaryItemResponse> ticketCategories,
            List<DictionaryItemResponse> priorities,
            List<RolePermissionResponse> rolePermissions
    ) {
    }
}
