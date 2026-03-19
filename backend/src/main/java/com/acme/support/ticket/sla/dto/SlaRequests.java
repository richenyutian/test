package com.acme.support.ticket.sla.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * SLA 请求对象。
 */
public final class SlaRequests {

    private SlaRequests() {
    }

    public record SlaRuleSaveRequest(
            @NotBlank String priorityCode,
            @NotNull @Min(1) Integer responseLimitMinutes,
            @NotNull @Min(1) Integer resolveLimitMinutes,
            @NotNull Integer enabledFlag,
            String remark
    ) {
    }
}
