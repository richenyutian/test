package com.acme.support.ticket.common.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 通用分页响应，适用于工单列表、通知列表、审计日志列表等场景。
 */
@Schema(description = "分页响应")
public record PageResponse<T>(
        @Schema(description = "当前页码") long current,
        @Schema(description = "每页条数") long size,
        @Schema(description = "总记录数") long total,
        @Schema(description = "列表数据") List<T> records
) {
}
