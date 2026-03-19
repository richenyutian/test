package com.acme.support.ticket.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一接口响应体，便于前后端在真实项目中保持稳定契约。
 */
@Schema(description = "统一接口响应")
public record ApiResponse<T>(
        @Schema(description = "业务状态码") String code,
        @Schema(description = "响应消息") String message,
        @Schema(description = "响应数据") T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "操作成功", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }
}
