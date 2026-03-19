package com.acme.support.ticket.common.enums;

public enum TicketUrgencyLevel {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    CRITICAL("紧急");

    private final String label;

    TicketUrgencyLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
