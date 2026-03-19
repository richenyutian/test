package com.acme.support.ticket.common.enums;

public enum TicketPriority {
    P1("极高"),
    P2("高"),
    P3("中"),
    P4("低");

    private final String label;

    TicketPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
