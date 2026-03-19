package com.acme.support.ticket.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单流转记录实体。
 */
@Data
@TableName("ticket_flow_record")
public class TicketFlowRecordEntity {

    @TableId(type = IdType.AUTO)
    private Long flowRecordId;
    private Long ticketId;
    private String actionCode;
    private String fromStatus;
    private String toStatus;
    private Long operatorUserId;
    private String operatorName;
    private String operateDescription;
    private LocalDateTime operateTime;
    private String requestPath;
    private Integer deletedFlag;
}
