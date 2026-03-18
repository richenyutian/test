package com.example.ticketworkflow.model;

import java.util.List;

public record WorkflowNodeDefinition(
    String nodeKey,
    String nodeName,
    String assignee,
    List<FormFieldDefinition> formFields
) {
}
