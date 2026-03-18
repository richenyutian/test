package com.example.ticketworkflow.model;

import java.util.List;

public record WorkflowTemplateDefinition(
    List<FormFieldDefinition> startFormFields,
    List<WorkflowNodeDefinition> nodes
) {
}
