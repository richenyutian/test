package com.example.ticketsystem.entity;

import java.time.LocalDateTime;

public class TicketRecordEntity {

    private Long id;
    private Long ticketId;
    private Long nodeId;
    private String nodeName;
    private String nodeState;
    private String assignee;
    private String operator;
    private String actionType;
    private String comment;
    private Long selectedNextNodeId;
    private String selectedNextNodeName;
    private String submittedDataJson;
    private String mergedDataJson;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeState() {
        return nodeState;
    }

    public void setNodeState(String nodeState) {
        this.nodeState = nodeState;
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getSelectedNextNodeId() {
        return selectedNextNodeId;
    }

    public void setSelectedNextNodeId(Long selectedNextNodeId) {
        this.selectedNextNodeId = selectedNextNodeId;
    }

    public String getSelectedNextNodeName() {
        return selectedNextNodeName;
    }

    public void setSelectedNextNodeName(String selectedNextNodeName) {
        this.selectedNextNodeName = selectedNextNodeName;
    }

    public String getSubmittedDataJson() {
        return submittedDataJson;
    }

    public void setSubmittedDataJson(String submittedDataJson) {
        this.submittedDataJson = submittedDataJson;
    }

    public String getMergedDataJson() {
        return mergedDataJson;
    }

    public void setMergedDataJson(String mergedDataJson) {
        this.mergedDataJson = mergedDataJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
