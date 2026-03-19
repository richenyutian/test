package com.acme.support.ticket.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "登录请求")
public record LoginRequest(
        @NotBlank
        @Schema(description = "用户名", example = "service.desk")
        String username,
        @NotBlank
        @Schema(description = "密码", example = "ChangeMe123!")
        String password
) {
}
