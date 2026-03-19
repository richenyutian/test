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

    @TableId(type = IdType.AUTO)
    private Long ticketId;
    private String ticketNo;
    private String title;
    private String description;
    private Long requesterUserId;
    private String requesterName;
    private String contactPhone;
    private Integer externalUserFlag;
    private String sourceCode;
    private String ticketTypeCode;
    private String categoryCode;
    private String priorityCode;
    private String currentStatus;
    private Long currentHandlerUserId;
    private Long currentHandleGroupId;
    private Integer escalatedFlag;
    private String escalateReason;
    private LocalDateTime escalateTime;
    private Long escalateOperatorId;
    private Integer timeoutFlag;
    private Integer responseTimeoutFlag;
    private Integer resolveTimeoutFlag;
    private Integer responseWarnNotifiedFlag;
    private Integer resolveWarnNotifiedFlag;
    private Integer responseSlaMinutes;
    private Integer resolveSlaMinutes;
    private LocalDateTime responseDeadline;
    private LocalDateTime resolveDeadline;
    private LocalDateTime responseFinishedAt;
    private LocalDateTime resolveFinishedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime processedAt;
    private LocalDateTime userConfirmedAt;
    private LocalDateTime closedAt;
    private String suspendReason;
    private String closeReason;
    private String reopenReason;
    private String resolutionSummary;
    private Integer reopenCount;
    private Integer version;
    private Integer deletedFlag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
