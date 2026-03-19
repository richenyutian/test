package com.acme.support.ticket.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单主表实体。
 */
@Data
@TableName("ticket_order")
public class TicketEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String ticketId;
    private String ticketNo;
    private String title;
    private String description;
    private String requesterId;
    private String requesterName;
    private String contactPhone;
    private String customerName;
    private String departmentName;
    private String source;
    private String ticketType;
    private String categoryCode;
    private String categoryName;
    private String priority;
    private String urgencyLevel;
    private String status;
    private String currentAssigneeId;
    private String currentAssigneeName;
    private String currentGroupCode;
    private String currentGroupName;
    private LocalDateTime responseDeadline;
    private LocalDateTime resolveDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private LocalDateTime closedAt;
    private String satisfactionLevel;
    private String satisfactionComment;
    private Boolean escalated;
    private Boolean timeout;
    private Integer deleted;
}
