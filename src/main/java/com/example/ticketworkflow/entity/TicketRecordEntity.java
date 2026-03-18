package com.example.ticketworkflow.entity;

import java.time.LocalDateTime;

public class TicketRecordEntity {

    private Long id;
    private Long instanceId;
    private String processInstanceId;
    private String taskId;
    private String taskDefinitionKey;
    private String taskName;
    private String assignee;
    private String operator;
    private String action;
    private String comment;
    private String submittedFormData;
    private String mergedFormData;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSubmittedFormData() {
        return submittedFormData;
    }

    public void setSubmittedFormData(String submittedFormData) {
        this.submittedFormData = submittedFormData;
    }

    public String getMergedFormData() {
        return mergedFormData;
    }

    public void setMergedFormData(String mergedFormData) {
        this.mergedFormData = mergedFormData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
