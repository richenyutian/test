package com.acme.support.ticket.common.enums;

public enum TicketSource {
    MANUAL_SUBMISSION("用户手工提交"),
    SERVICE_DESK_ENTRY("客服代录入");

    private final String label;

    TicketSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
