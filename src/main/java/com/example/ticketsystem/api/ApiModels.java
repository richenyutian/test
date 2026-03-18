package com.example.ticketsystem.api;

import com.example.ticketsystem.model.FlowTransitionDefinition;
import com.example.ticketsystem.model.FormFieldDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ApiModels {

    private ApiModels() {
    }

    public record FormSaveRequest(
        Long id,
        String formCode,
        @NotBlank String name,
        String description,
        @NotEmpty List<FormFieldDefinition> fields
    ) {
    }

    public record FormView(
        Long id,
        String formCode,
        String name,
        String description,
        String status,
        List<FormFieldDefinition> fields,
        LocalDateTime updatedAt
    ) {
    }

    public record NodeSaveRequest(
        Long id,
        String nodeCode,
        @NotBlank String name,
        @NotBlank String nodeState,
        @NotNull Long formId,
        @NotBlank String assignee,
        @NotBlank String actionType,
        String description
    ) {
    }

    public record NodeView(
        Long id,
        String nodeCode,
        String name,
        String nodeState,
        Long formId,
        String formName,
        String assignee,
        String actionType,
        String description,
        List<FormFieldDefinition> formFields,
        LocalDateTime updatedAt
    ) {
    }

    public record FlowSaveRequest(
        Long id,
        String flowCode,
        @NotBlank String name,
        String description,
        @NotNull Long startNodeId,
        @NotEmpty List<Long> nodeIds,
        @NotEmpty List<FlowTransitionDefinition> transitions
    ) {
    }

    public record FlowNodeView(
        Long id,
        String name,
        String nodeState,
        Long formId,
        String formName,
        String assignee,
        String actionType,
        List<FormFieldDefinition> formFields
    ) {
    }

    public record FlowTransitionView(
        Long fromNodeId,
        String fromNodeName,
        Long toNodeId,
        String toNodeName,
        String transitionName,
        Integer sortNo
    ) {
    }

    public record FlowView(
        Long id,
        String flowCode,
        String name,
        String description,
        String status,
        Long startNodeId,
        List<FlowNodeView> nodes,
        List<FlowTransitionView> transitions,
        LocalDateTime updatedAt
    ) {
    }

    public record StartTicketRequest(
        @NotNull Long flowId,
        @NotBlank String title,
        @NotBlank String applicant,
        Map<String, Object> formData,
        Long nextNodeId
    ) {
    }

    public record CompleteTaskRequest(
        @NotBlank String operator,
        String comment,
        Map<String, Object> formData,
        Long nextNodeId
    ) {
    }

    public record NextNodeOptionView(
        Long nodeId,
        String nodeName,
        String nodeState
    ) {
    }

    public record TaskView(
        Long taskId,
        Long ticketId,
        String ticketNo,
        String title,
        Long nodeId,
        String nodeName,
        String nodeState,
        String assignee,
        String actionType,
        Long formId,
        String formName,
        List<FormFieldDefinition> formFields,
        Map<String, Object> businessData,
        List<NextNodeOptionView> nextNodeOptions,
        LocalDateTime startedAt
    ) {
    }

    public record TicketView(
        Long id,
        String ticketNo,
        String flowName,
        String title,
        String applicant,
        String currentNodeName,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime finishedAt
    ) {
    }

    public record TicketRecordView(
        Long id,
        String nodeName,
        String nodeState,
        String assignee,
        String operator,
        String actionType,
        String comment,
        Long selectedNextNodeId,
        String selectedNextNodeName,
        Map<String, Object> submittedData,
        Map<String, Object> mergedData,
        LocalDateTime createdAt
    ) {
    }

    public record TicketDetailView(
        Long id,
        String ticketNo,
        String flowName,
        String title,
        String applicant,
        Long currentNodeId,
        String currentNodeName,
        String status,
        Map<String, Object> businessData,
        List<TicketRecordView> records,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime finishedAt
    ) {
    }
}
