package com.example.ticketworkflow.api;

import com.example.ticketworkflow.model.FormFieldDefinition;
import com.example.ticketworkflow.model.WorkflowNodeDefinition;
import com.example.ticketworkflow.model.WorkflowTemplateDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ApiModels {

    private ApiModels() {
    }

    public record TemplateSaveRequest(
        Long id,
        String templateCode,
        @NotBlank String name,
        String description,
        List<FormFieldDefinition> startFormFields,
        List<WorkflowNodeDefinition> nodes
    ) {
    }

    public record WorkflowTemplateView(
        Long id,
        String templateCode,
        String name,
        String description,
        String status,
        Integer versionNo,
        String processDefinitionId,
        WorkflowTemplateDefinition definition,
        LocalDateTime updatedAt
    ) {
    }

    public record StartProcessRequest(
        @NotNull Long templateId,
        @NotBlank String title,
        @NotBlank String initiator,
        Map<String, Object> formData
    ) {
    }

    public record CompleteTaskRequest(
        @NotBlank String operator,
        String comment,
        Map<String, Object> formData
    ) {
    }

    public record TicketRecordView(
        Long id,
        String taskDefinitionKey,
        String taskName,
        String assignee,
        String operator,
        String action,
        String comment,
        Map<String, Object> submittedFormData,
        Map<String, Object> mergedFormData,
        LocalDateTime createdAt
    ) {
    }

    public record TicketFieldView(
        String fieldKey,
        String fieldLabel,
        String fieldValue
    ) {
    }

    public record TicketInstanceView(
        Long id,
        String businessKey,
        String templateName,
        String title,
        String initiator,
        String currentNodeName,
        String status,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime updatedAt
    ) {
    }

    public record TicketInstanceDetailView(
        Long id,
        String businessKey,
        Long templateId,
        String templateName,
        String title,
        String initiator,
        String currentNodeName,
        String status,
        Map<String, Object> mergedFormData,
        List<TicketFieldView> indexedFields,
        List<TicketRecordView> records,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime updatedAt
    ) {
    }

    public record TaskView(
        String taskId,
        String taskName,
        String nodeKey,
        String assignee,
        Long instanceId,
        String businessKey,
        String instanceTitle,
        String initiator,
        List<FormFieldDefinition> formFields,
        Map<String, Object> mergedFormData,
        LocalDateTime createdAt
    ) {
    }
}
