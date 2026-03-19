package com.acme.support.ticket.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "登录请求")
public record LoginRequest(
        @NotBlank
        @Schema(description = "用户名，当前阶段采用开发/测试模式模拟 SSO 直登", example = "admin")
        String username
) {
}
