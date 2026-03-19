package com.acme.support.ticket.common.enums;

/**
 * 工单生命周期状态枚举。
 */
public enum TicketStatus {
    PENDING_ACCEPT("待受理"),
    ACCEPTED("已受理"),
    PENDING_ASSIGN("待分派"),
    PROCESSING("处理中"),
    PENDING_CONFIRM("待用户确认"),
    COMPLETED("已完成"),
    CLOSED("已关闭"),
    CANCELLED("已取消"),
    SUSPENDED("已挂起"),
    REOPENED("已重开");

    private final String label;

    TicketStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
