package com.acme.support.ticket.ticket.query;

/**
 * 工单列表查询对象。
 */
public record TicketQuery(
        String keyword,
        String currentStatus,
        String priorityCode,
        Long handleGroupId,
        Long handlerUserId
) {
}
