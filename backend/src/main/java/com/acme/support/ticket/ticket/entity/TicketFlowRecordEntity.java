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

    @TableId(type = IdType.ASSIGN_UUID)
    private String recordId;
    private String ticketId;
    private String actionType;
    private String operatorId;
    private String operatorName;
    private String fromStatus;
    private String toStatus;
    private String remark;
    private LocalDateTime operatedAt;
    private Integer deleted;
}
