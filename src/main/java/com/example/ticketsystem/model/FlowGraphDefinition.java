package com.example.ticketsystem.model;

import java.util.List;

public record FlowGraphDefinition(
    List<Long> nodeIds,
    List<FlowTransitionDefinition> transitions
) {
}
