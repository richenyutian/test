package com.acme.support.ticket.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public final class AuthResponses {

    private AuthResponses() {
    }

    @Schema(description = "登录响应")
    public record LoginResponse(
            @Schema(description = "访问令牌") String accessToken,
            @Schema(description = "令牌类型") String tokenType,
            @Schema(description = "用户信息") UserProfileResponse profile
    ) {
    }

    @Schema(description = "当前登录用户")
    public record UserProfileResponse(
            Long userId,
            String username,
            String displayName,
            String userType,
            List<String> roles,
            List<String> menuPermissions,
            List<String> buttonPermissions,
            String dataScope
    ) {
    }
}
