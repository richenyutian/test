package com.example.ticketsystem.model;

public record FlowTransitionDefinition(
    Long fromNodeId,
    Long toNodeId,
    String transitionName,
    Integer sortNo
) {
}
